package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.*;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {
    @SuppressWarnings("unused")
    public static void removePlayerFromGame(Player player, DragonGame game) {
        Wrapper wrapper = game.getWrapper();
        for (GameMap map : wrapper.maps) {
            boolean wasIngame = false;
            if (wrapper.isQueuedOrIngameOrSpectating(player)) wasIngame = true;
            if (map.queue.queue.contains(player)) {
                player.sendMessage(Main.colorize("&aYou are no longer in the queue for &f" + game.getName() + " &aon &f" + map.name + "&a."));
                map.queue.queue.remove(player);
            }
            for (Team t : map.queue.getTeams()) {
                if (t.getPlayers().contains(player)) {
                    player.sendMessage(Main.colorize("&aYou are no longer in &f" + game.getName() + " &aon &f" + map.name + "&a."));
                    t.removePlayer(player);
                    Debug.verbose("Removed " + player.getName() + " from the team " + t.getTeamName() + "&r.");
                    break;
                }
            }
            if (map.queue.spectators.contains(player)) {
                player.sendMessage(Main.colorize("&aYou are no longer spectating in &f" + game.getName() + " &aon &f" + map.name + "&a."));
                map.queue.spectators.remove(player);
            }
            if (wasIngame) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            if (!player.getWorld().getName().equalsIgnoreCase("world")) {
                Wrapper.clearInventory(player.getInventory());
                player.setGameMode(GameMode.SURVIVAL);
                Wrapper.teleportToSpawn(player);
            }
        }
        wrapper.updatePlayerCounts();
        wrapper.cancelGamesIfUnderPlayerCount();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Debug.verbose("Removing player from games (with /leave): " + player.getName());
            for (DragonGame game : Main.games) {
                removePlayerFromGame(player, game);
            }
            return true;
        } else {
            sender.sendMessage(Main.colorize("&cYou must be a player to use this command."));
            return true;
        }
    }
}
