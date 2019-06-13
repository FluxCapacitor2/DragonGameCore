package me.fluxcapacitor.dragongamecore;

import com.connorlinfoot.titleapi.TitleAPI;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListeners implements Listener {

    private final DragonGame game;
    private final Wrapper wrapper;

    EventListeners(DragonGame game) {
        this.game = game;
        this.wrapper = game.getWrapper();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (wrapper.isIngame(event.getPlayer())) {
            Debug.verbose(event.getPlayer().getName() + " has respawned. They are ingame.");
            new BukkitRunnable() {
                @Override
                public void run() {
                    game.getGameLifecycle().onPlayerRespawn(game, game.getMap(event.getPlayer()), event.getPlayer(), event);
                }
            }.runTaskLater(game.getInstance(), 1L);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            Player player = (Player) event.getEntity();
            for (GameMap map : wrapper.maps) {
                if (wrapper.isIngame(player, map)) {
                    Debug.verbose("Player death event was triggered for " + player.getName() + " on " + map.name + " in " + map.getGame().getName() + " (Killer: " + player.getKiller() + ")");
                    game.getGameLifecycle().onPlayerKill(game, map, player, player.getKiller(), event);
                    if (game.isSpectatingEnabled()) {
                        Debug.verbose("Spectating is enabled. " + player.getName() + " will be removed from their team & added as a spectator.");
                        map.queue.removePlayerFromTeams(player);
                        map.queue.spectators.add(player);
                        //There's 2+ people still in the game.
                        //Allow them to spectate.
                        if (map.queue.getTeamCountWithPlayers() > 1) {
                            Debug.verbose("There are enough people left in the game to allow " + player.getName() + " to spectate.");
                            //Set them into spectator mode & teleport them to the arena.
                            //Also tell them how to get out
                            //Teleport them to the arena's spawn point in spectator mode so they can watch
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.setGameMode(GameMode.SPECTATOR);
                                    TitleAPI.sendTitle(player, 20, 80, 20,
                                            Main.colorizeWithoutPrefix("&aYou are now spectating"),
                                            Main.colorizeWithoutPrefix("&aTo leave, type &f/leave&a."));
                                    player.teleport(map.getSpawnPoint(player));
                                }
                            }.runTaskLater(game.getInstance(), 2L);
                        } else {
                            Debug.verbose(player.getName() + " was the last in the game. They were not teleported or put into spectator mode.");
                        }
                        //If there was only 1 player left, the player counts will be updated & the game will continue OR end.
                        wrapper.updatePlayerCounts();
                    }
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (GameMap map : wrapper.maps) {
            map.queue.queue.remove(player);
            map.queue.removePlayerFromTeams(player);
            map.queue.spectators.remove(player);
            wrapper.updatePlayerCounts();
            wrapper.cancelGamesIfUnderPlayerCount();
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onBlockBreak(BlockBreakEvent event) {
        for (GameMap map : wrapper.maps) {
            if (wrapper.isIngame(event.getPlayer(), map) && map.queue.timerStarted && !map.queue.gameStarted) {
                //The player's game hasn't started, so cancel the event
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onBlockPlace(BlockPlaceEvent event) {
        for (GameMap map : wrapper.maps) {
            if (wrapper.isIngame(event.getPlayer(), map) && map.queue.timerStarted && !map.queue.gameStarted) {
                //The player's game hasn't started, so cancel the event
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        if (wrapper.isIngame((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (wrapper.isIngame(event.getEntity())) event.getDrops().clear();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setMaxHealth(20.0D);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerHungerDeplete(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        ((Player) event.getEntity()).setFoodLevel(20);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (event.getEntityType().equals(EntityType.PLAYER) && !cause.equals(EntityDamageEvent.DamageCause.LAVA) && !cause.equals(EntityDamageEvent.DamageCause.VOID)) {
            Player player = (Player) event.getEntity();
            DragonGame game = Wrapper.findGame(player);
            if (game != null && game.isDisableDamage()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerChat(PlayerCommandPreprocessEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            if (!player.hasPermission("arcade.commandsingame") && wrapper.isIngame(player)) {
                String message = event.getMessage();
                if (!message.startsWith("/leave") && !message.startsWith("/dragongamecore:leave")) {
                    player.sendMessage(Main.colorize("&cOnly &f/leave&c is allowed here!"));
                    event.setCancelled(true);
                }
            }
        }
    }
}
