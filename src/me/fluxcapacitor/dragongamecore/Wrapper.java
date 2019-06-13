package me.fluxcapacitor.dragongamecore;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class Wrapper {
    public ArrayList<GameMap> maps = new ArrayList<>();

    public Wrapper() {
        super();
    }

    public static DragonGame findGame(String query) {
        for (DragonGame game : Main.games) {
            if (game.getName().equalsIgnoreCase(query)) {
                return game;
            }
        }
        return null;
    }

    public static void clearInventory(PlayerInventory inv) {
        inv.clear();
        inv.setHelmet(new ItemStack(Material.AIR));
        inv.setChestplate(new ItemStack(Material.AIR));
        inv.setLeggings(new ItemStack(Material.AIR));
        inv.setBoots(new ItemStack(Material.AIR));
    }

    public static void teleportToSpawn(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        TitleAPI.clearTitle(player);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
    }

    public static DragonGame findGame(Player player) {
        for (DragonGame game : Main.games) {
            if (game.getWrapper().isIngame(player)) return game;
        }
        return null;
    }

    public Team getTeam(Player player) {
        for (GameMap map : this.maps) {
            for (Team team : map.queue.getTeams()) {
                for (Player p : team.getPlayers()) {
                    if (p.equals(player)) return team;
                }
            }
        }
        return null;
    }

    public void dumpMaps() {
        Bukkit.getConsoleSender().sendMessage("---------- [MAP DUMP] ----------");
        for (GameMap map : maps) {
            Bukkit.getConsoleSender().sendMessage("[MAP: " + map.name + "]");
            Bukkit.getConsoleSender().sendMessage(map.toString());
        }
        Bukkit.getConsoleSender().sendMessage("---------- [MAP DUMP] ----------");
    }

    public boolean isQueuedOrIngameOrSpectating(Player player) {
        for (GameMap map : this.maps) {
            if (map.queue.queue.contains(player) | map.queue.spectators.contains(player)) {
                return true;
            }
            for (Team t : map.queue.getTeams()) {
                if (t.getPlayers().contains(player)) return true;
            }
        }
        return false;
    }

    public boolean isSpectating(Player player) {
        for (GameMap map : this.maps) {
            if (map.queue.spectators.contains(player)) {
                return true;
            }
        }
        return false;
    }

    /*
    public boolean isQueued(Player player) {
        for (GameMap map : this.maps) {
            if (map.queue.queue.contains(player)) {
                return true;
            }
        }
        return false;
    }
    */
    public boolean isIngame(Player player) {
        for (GameMap map : this.maps) {
            for (Team t : map.queue.getTeams()) {
                if (t.getPlayers().contains(player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isIngame(Player player, GameMap map) {
        for (Team t : map.getTeams()) {
            if (t.getPlayers().contains(player)) return true;
        }
        return false;
    }

    public GameMap findMap(String name) {
        for (GameMap map : maps) {
            if (map.name.equalsIgnoreCase(name)) {
                return map;
            }
        }
        return getRandomMap();
    }

    private GameMap getRandomMap() {
        if (this.maps.size() > 0)
            return this.maps.get(new Random().nextInt(this.maps.size()));
        else Debug.error("There are no maps defined for a game that a player has requested to join. " +
                "Use /aadebug and/or /maps <game> to figure out which games don't have maps defined.");
        return null;
    }

    public GameMap findMostPopulatedMap() {
        int max = 0;
        GameMap bestMap = getRandomMap();
        //Find the best map by checking for maps that have a started timer, but the game has not started yet. This is the perfect time to join (because there's less waiting).
        ArrayList<GameMap> mapsList = maps;
        Collections.shuffle(mapsList);
        for (GameMap map : mapsList) {
            if (map.queue.timerStarted && !map.queue.gameStarted && map.queue.getPlayerCount() > max && map.queue.getTeams().size() < map.queue.MAX_PLAYERS_PER_LOBBY) {
                max = map.queue.getPlayerCount();
                bestMap = map;
            }
        }

        //There's nobody playing in a game that hasn't started. Resort to less resirable games instead of random ones.
        //Find maps with the most queued players under the limit.
        if (max == 0) {
            for (GameMap map : mapsList) {
                if (!map.queue.gameStarted && map.queue.queue.size() > max && map.queue.queue.size() < map.queue.MAX_PLAYERS_PER_LOBBY) {
                    max = map.queue.queue.size();
                    bestMap = map;
                }
            }
        }

        return bestMap;
    }

    public void updatePlayerCounts() {
        for (GameMap map : maps) {
            //Remove all offline players from the games/lobbies
            Iterator iterator1, iterator2, iterator3;
            for (iterator1 = map.queue.queue.iterator(); iterator1.hasNext(); ) {
                if (!((Player) iterator1.next()).isOnline()) iterator1.remove();
            }
            for (Team t : map.queue.getTeams()) {
                for (iterator2 = t.getPlayers().iterator(); iterator2.hasNext(); ) {
                    if (!((Player) iterator2.next()).isOnline()) iterator1.remove();
                }
            }
            for (iterator3 = map.queue.spectators.iterator(); iterator3.hasNext(); ) {
                if (!((Player) iterator3.next()).isOnline()) iterator3.remove();
            }
            //Check if there's a winner
            if (map.getGame().isFFA()) {
                //This is an FFA game
                if (map.queue.getTeams().size() == 1 && map.queue.gameStarted && map.queue.canDeclareWinner) {
                    //We have a winner!
                    this.declareWinner(map, map.queue.getTeams().get(0));
                }
            } else {
                //This game is a team game.
                if (map.queue.gameStarted && map.queue.canDeclareWinner) {
                    ArrayList<Team> allCurrentTeams = new ArrayList<>();
                    for (Team team : map.queue.getTeams()) {
                        if (team.getPlayers().size() > 0) {
                            allCurrentTeams.add(team);
                        }
                    }
                    if (allCurrentTeams.size() == 1) {
                        //We have a winner! (There's only 1 team with players still in the game)
                        Debug.verbose("Declaring winner (Caused by Wrapper#updatePlayerCounts)");
                        declareWinner(map, allCurrentTeams.get(0));
                    }
                }
            }
        }
    }

    private void declareWinner(GameMap map, Team t) {
        Debug.info("The team " + t.getTeamName() + " has won the game of " + map.getGame().getName() + " on " + map.name + ".");
        Debug.verbose("The players on " + t.getTeamName() + " include: " + t.getPlayers().toString());
        ArrayList<Player> winners = new ArrayList<>();
        for (Iterator it = t.getPlayers().iterator(); it.hasNext(); ) {
            Player winner = (Player) it.next();
            //Trigger game-specific on win method
            map.getGame().getGameLifecycle().onPlayerWin(map.getGame(), map, winner);
            //Add them as a "spectator" so we can just display the win message to all spectators.
            map.queue.spectators.add(winner);
            winners.add(winner);
            //Remove them from their team
            it.remove();
        }
        for (Player spectator : map.queue.spectators) {
            //Send them a message saying who won
            spectator.sendMessage(Main.colorizeWithoutPrefix("&f&l&m--------------------\n" +
                    "&e&l" + map.getGame().getName() + "\n" +
                    "\n" +
                    "&f&lWinner: &e&l" + t.getTeamName() + "\n" +
                    "\n" +
                    "&f&l&m--------------------"));
            //Clear their inventory and armor slots
            clearInventory(spectator.getInventory());
            //Reset the max health (for FastFall & other games)
            spectator.setMaxHealth(20.0D);
            spectator.setHealth(20.0D);
            //Remove their potion effects
            spectator.removePotionEffect(PotionEffectType.SATURATION);
            spectator.removePotionEffect(PotionEffectType.REGENERATION);
            //Set them into survival mode
            spectator.setGameMode(GameMode.SURVIVAL);
            //Teleport them to spawn
            teleportToSpawn(spectator);
            //Clear their titles (for nonwinners) in case they have the "YOU ARE NOW SPECTATING" title still up to avoid confusion.
            if (!winners.contains(spectator)) {
                TitleAPI.clearTitle(spectator);
            }
        }
        for (Player winner : winners) {
            Debug.verbose("Sending winning title to " + winner.getName());
            TitleAPI.sendTitle(winner, 20, 100, 20,
                    Main.colorizeWithoutPrefix("&6&lWINNER"),
                    Main.colorizeWithoutPrefix(map.getGame().getWinnerSubtitle())
            );
        }
        //If there are enough people in queue, reset & start the next game.
        map.queue.resetVariables();
        map.queue.setupNextGame();
        //Turn off declaring a winner until the next game to prevent "spamming" of this method.
        map.queue.canDeclareWinner = false;
    }

    public void cancelGamesIfUnderPlayerCount() {
        for (GameMap map : maps) {
            //Check if there's less players needed to start than required & the game hasn't started
            if (map.queue.getTeams().size() < map.queue.START_REQUIREMENT && !map.queue.gameStarted && map.queue.timerStarted) {
                //Cancel the current game
                map.queue.timer.cancelTimer();
                map.queue.resetVariables(true);
                map.queue.setupNextGame();
                //Tell everyone what happened, teleport them back to spawn, and remove them from the game.
                for (Team t : map.queue.getTeams()) {
                    for (Iterator<Player> it = t.getPlayers().iterator(); it.hasNext(); ) {
                        Player player = it.next();
                        //Tell them what happened
                        TitleAPI.sendTitle(player, 20, 100, 20, Main.colorizeWithoutPrefix("&cCANCELLED"), Main.colorizeWithoutPrefix("&cThere aren't enough players to start!"));
                        player.sendMessage(Main.colorize("&cYour game was cancelled because of a lack of players."));
                        //Send them to spawn
                        teleportToSpawn(player);
                        //Remove them from the game
                        it.remove();
                    }
                }
            }
        }
    }
}
