package me.fluxcapacitor.dragongamecore;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Queue {
    final int START_REQUIREMENT;
    final int MAX_PLAYERS_PER_LOBBY;
    private final int COUNTDOWN_TIME;
    private final GameMap map;
    private final DragonGame game;
    private final GameLifecycle gameLifecycle;
    private final String gameStartSubtitle;
    public ArrayList<Player> queue;
    //public ArrayList<Player> ingame;
    public ArrayList<Team> teams;
    @SuppressWarnings("WeakerAccess")
    public boolean timerStarted;
    @SuppressWarnings("WeakerAccess")
    public boolean gameStarted;
    public ArrayList<Player> spectators;
    CountdownTimer timer;
    boolean canDeclareWinner;

    Queue(DragonGame game, GameMap map, int countdownTime, int maxPlayers, int startRequirement) {
        this.queue = new ArrayList<>();
        //this.ingame = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.timerStarted = false;
        this.gameStarted = false;
        this.canDeclareWinner = false;
        this.map = map;
        this.game = game;
        this.gameLifecycle = game.getGameLifecycle();
        this.gameStartSubtitle = game.getStartSubtitle();
        if (Main.isBeta()) COUNTDOWN_TIME = 10;
        else COUNTDOWN_TIME = countdownTime;
        START_REQUIREMENT = startRequirement;
        MAX_PLAYERS_PER_LOBBY = maxPlayers;
    }

    public void add(Player player) {
        //Make sure they aren't already queueing for something else
        for (DragonGame game1 : Main.games) {
            for (GameMap map : game1.getWrapper().maps) {
                if (map.queue.queue.contains(player)) {
                    player.sendMessage(Main.colorize("&cYou are already queued for &f" + game1.getName() + "&c on &f" + map.name + "&c! Type &f/leave&c to leave that queue before joining another one."));
                    return;
                }
            }
        }
        if (!this.gameStarted && this.timerStarted && this.teams.size() + 1 <= MAX_PLAYERS_PER_LOBBY) {
            //They can join a game that hasn't started yet!
            //Do all of the stuff we need to do for each player (like sending titles & putting them into adventure mode)
            pregamePerPlayer(game, map, player);
            //Take 2 seconds off the timer by calling run() twice (which takes off 1 second each)
            this.timer.run();
            this.timer.run();
        } else {
            //There's already a game being played. They will just chill in the queue for now.
            if (player.hasPermission("arcade.priorityqueue")) {
                //Put them in front of people without priority queue

                for (int i = 0; i < this.queue.size(); i++) {
                    Player p = this.queue.get(i);
                    if (!p.hasPermission("arcade.priorityqueue")) {
                        this.queue.add(p);
                        break;
                    }
                }
                this.queue.add(0, player);
            } else {
                //"Back of the line, commoner!"
                this.queue.add(player);
            }
            player.sendMessage(Main.colorize("&aYou have queued for &f" + game.getName() + "&a on &f" + map.name + "&a. You are at position " +
                    "&f" + (this.queue.indexOf(player) + 1) + "&a/&f" + this.queue.size() + "&a in the queue."));
        }
        if (this.queue.size() == START_REQUIREMENT && !this.gameStarted && !this.timerStarted) {
            this.startTimer();
        }
    }

    String getWorldName() {
        return this.map.getBlocks().get(0).getWorldName();
    }

    private void preGame() {
        //Reset the map
        this.map.reset();
        //Put the top 10 players in the queue into the game
        for (int i = 0; i < MAX_PLAYERS_PER_LOBBY; i++) {
            if (this.queue.size() >= 1) {
                //Should be in the array bounds...
                //Transfer them from the queue to in the game.
                if (game.isFFA()) {
                    this.addTeam(new Team(this.queue.get(0)));
                }
                pregamePerPlayer(game, map, this.queue.get(0));
                this.queue.remove(0);
            }
        }
        //Make everyone invincible
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag __global__ invincible -w " + map.queue.getWorldName() + " allow");
        //Run game-specific pregame method.
        gameLifecycle.pregame(game, map);
    }

    private void pregamePerPlayer(DragonGame game, GameMap map, Player player) {
        Debug.verbose("Running pregame per-player for " + player.getName());
        //Is this game free-for-all or are there already-defined teams?
        if (game.isFFA()) {
            //If it's FFA, make a team under the player's name
            Team newTeam = new Team(player);
            //Make sure the "new" team is not a duplicate
            if (!newTeam.doesMatchFromList(this.teams)) {
                //It's not a duplicate. Go ahead & add it!
                Debug.info("Creating new team for FFA player: " + newTeam.getTeamName());
                this.teams.add(newTeam);
            }
        } else {
            Team preferredTeam = null;
            try {
                FileConfiguration config = Main.instance.getConfig();
                File file = game.getMapsFile();
                config.load(file);
                String query = (String) config.get("playerdata.preferredTeams." + player.getUniqueId());
                for (Team t : teams) {
                    if (ChatColor.stripColor(t.getTeamName()).equals(ChatColor.stripColor(query))) {
                        //This is their preferred team!
                        preferredTeam = t;
                    }
                }
            } catch (IOException | InvalidConfigurationException e) {
                Debug.warn("There was an error saving / loading the config file for " + game.getName() + ".");
                Debug.warn("The error is printed below.");
                Debug.warn(e.getMessage());
                e.printStackTrace();
            }

            if (preferredTeam != null) {
                preferredTeam.addPlayer(player);
            } else {
                //They didn't select a preferred team: assign them to the team with the least players on it.
                int min = this.teams.get(0).getPlayers().size();
                Team best = this.teams.get(0);
                for (Team t : this.teams) {
                    if (t.getPlayers().size() < min) {
                        min = t.getPlayers().size();
                        best = t;
                    }
                }
                best.addPlayer(player);
                Debug.verbose("Added " + player.getName() + " to the team " + best.getTeamName());
            }
        }
        //Teleport them to the map's spawn point
        try {
            Location spawnPoint = this.getSpawnPoint(player);
            player.teleport(spawnPoint);
        } catch (NullPointerException e) {
            for (Team t : this.teams) {
                for (Player pl : t.getPlayers()) {
                    pl.sendMessage(Main.colorize("&aThe creator of this map did not set a spawn point! Tell a server administrator to fix this."));
                    Debug.error("There is no spawn point set for the map " + map.name + " in the game " + game.getName() + "!");
                }
            }
        }
        //Heal everyone & set their max health to normal
        player.setMaxHealth(20.0D);
        player.setHealth(20.0D);
        //Run the game-specific pregame per-player method
        gameLifecycle.pregamePerPlayer(game, map, player);
    }

    private Location getSpawnPoint(Player player) {
        return this.map.getSpawnPoint(player);
    }

    @Override
    public String toString() {
        return "\n       Ingame: " + this.teams.toString() +
                "\n       Spectating: " + this.spectators.toString() +
                "\n       Queued: " + this.queue.toString();
    }

    private void startGame() {
        //Balance teams
        this.balanceTeams();
        //Turn off friendly fire
        for (Team t : teams) {
            t.removeFriendlyFire();
        }
        //Reset the players' invincibility
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag __global__ invincible -w " + map.queue.getWorldName());
        for (Team t : this.teams) {
            for (Player p : t.getPlayers()) {
                TitleAPI.sendTitle(p, 20, 60, 20, Main.colorizeWithoutPrefix("&a&lGO"), Main.colorizeWithoutPrefix(this.gameStartSubtitle));
            }
        }
        //Run game-specific on game start method
        gameLifecycle.onGameStart(game, map);
        for (Team t : this.teams) {
            for (Player p : t.getPlayers()) {
                gameLifecycle.onGameStartPerPlayer(game, map, p);
            }
        }
        //To start the code that declares a winner on death and other things, set the game started to true
        this.gameStarted = true;
        this.canDeclareWinner = true;
    }

    private void balanceTeams() {
        for (Team x : this.teams) {
            for (Team y : this.teams) {
                if (x.getPlayers().size() >= y.getPlayers().size() + 2) {
                    //The team is unbalanced. Move a player from x to y
                    Player last = x.getPlayers().get(x.getPlayers().size() - 1);
                    x.removePlayer(last);
                    y.addPlayer(last);
                    last.teleport(map.getSpawnPoint(y));
                    last.sendMessage(Main.colorize("&aDue to team balancing, you were moved to the " + y.getTeamName() + "&r&a team."));
                    //If they're still unbalanced, call the method again because we won't get to come back to this.
                    if (x.getPlayers().size() >= y.getPlayers().size() + 2) balanceTeams();
                }
            }
        }
    }

    private void countDown(CountdownTimer t) {
        int secondsLeft = t.getSecondsLeft();
        gameLifecycle.everySecond(game, map, t);
        if (secondsLeft % 10 == 0 || secondsLeft <= 5) {
            for (Team team : this.teams) {
                for (Player p : team.getPlayers()) {
                    TitleAPI.sendTitle(p, 20, 20, 20, Main.colorizeWithoutPrefix("&f" + secondsLeft + "&a seconds!"), Main.colorizeWithoutPrefix("&aType &f/leave&a to leave the game."));
                    gameLifecycle.everySecondPerPlayer(game, map, t, p);
                }
            }
        }
    }

    void setupNextGame() {
        if (this.teams.size() == 0) {
            //The game is over & the winner was removed from the game as well.
            //Move the next 10 people from the queue into a game (IF there are enough people in the queue)
            if (this.queue.size() >= START_REQUIREMENT) {
                //There is enough people in queue for this map. It is okay to start the pregame again.
                // Queue#startTimer() will run Queue#preGame() as soon as it is triggered, so we don't need to put that here.
                this.startTimer();
                this.canDeclareWinner = false;
            }
            //If this did not trigger, then there aren't enough people to start. When someone joins, it will be handled by `Queue#add(Player)`
        }
    }

    private void startTimer() {
        //Code adapted from a forum post by @ExpDev on SpigotMC.org
        this.timer = this.newTimer();
        this.timerStarted = true;
        timer.scheduleTimer();
        gameLifecycle.onTimerStart(game, map);
        for (Team t : this.teams) {
            for (Player p : t.getPlayers()) {
                gameLifecycle.onTimerStartPerPlayer(game, map, p);
            }
        }
    }

    private CountdownTimer newTimer() {
        return new CountdownTimer(Main.instance, COUNTDOWN_TIME, this::preGame, this::startGame, this::countDown);
    }

    void resetVariables(boolean preserveQueue) {
        for (Team t : teams) {
            t.team.unregister();
        }
        this.canDeclareWinner = false;
        this.timerStarted = false;
        this.gameStarted = false;
        if (!preserveQueue) {
            this.queue = new ArrayList<>();
            this.teams = new ArrayList<>();
            this.spectators = new ArrayList<>();
        }
        if (this.timer != null) this.timer.cancelTimer();
        this.timer = this.newTimer();
        this.map.reset();
        this.removeAllTeams();
    }

    public void resetVariables() {
        resetVariables(false);
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    private void addTeam(Team team) {
        this.teams.add(team);
    }

    private void removeAllTeams() {
        this.teams = new ArrayList<>();
    }

    public int getTeamCountWithPlayers() {
        int count = 0;
        for (Team t : this.teams) {
            if (t.getPlayers().size() > 0) {
                count++;
            }
        }
        return count;
    }

    public int getPlayerCount() {
        int count = 0;
        for (Team t : this.teams) {
            count += t.getPlayers().size();
        }
        return count;
    }

    @SuppressWarnings("unused")
    public void declareWinner(Player player) {
        if (canDeclareWinner) {
            teams.forEach(o -> map.queue.spectators.addAll(o.getPlayers()));
            spectators.remove(player);
            if (game.isFFA()) {
                teams.removeIf(t -> !t.players.get(0).getUniqueId().equals(player.getUniqueId()));
            } else {
                for (Team t : teams) {
                    if (!t.players.contains(player)) {
                        teams.remove(t);
                    }
                }
            }
            Debug.verbose("A winner was declared for " + game.getName() + " on " + map.name + ".");
        } else {
            Debug.verbose(game.getName() + " on " + map.name + " tried to declare a winner, but was not allowed to.");
        }
        game.getWrapper().updatePlayerCounts();
        removeAllTeams();
        setupNextGame();
    }

    public void removePlayerFromTeams(Player player) {
        for (Iterator it = teams.iterator(); it.hasNext(); ) {
            Team t = (Team) it.next();
            t.removePlayer(player);
            Debug.verbose(player.getName() + " was removed from their teams.");
            if (game.isFFA() && t.getPlayers().size() == 0) {
                it.remove();
                Debug.verbose("The team " + t.getTeamName() + " was removed because there were no players after the removal of " + player.getName() + ".");
            }
        }
    }
}
