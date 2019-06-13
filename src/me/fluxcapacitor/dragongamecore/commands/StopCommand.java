package me.fluxcapacitor.dragongamecore.commands;

import com.connorlinfoot.titleapi.TitleAPI;
import me.fluxcapacitor.dragongamecore.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class StopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<DragonGame> games = new ArrayList<>();
        if (args.length > 0 && Wrapper.findGame(args[0]) != null) {
            games.add(Wrapper.findGame(args[0]));
        } else {
            games.addAll(Main.games);
        }
        for (DragonGame game : games) {
            for (GameMap map : game.getWrapper().maps) {
                ArrayList<Player> allPlayers = map.queue.queue;
                allPlayers.addAll(map.queue.spectators);
                for (Team t : map.queue.getTeams()) allPlayers.addAll(t.getPlayers());

                for (Player player : allPlayers) {
                    TitleAPI.sendTitle(player, 20, 100, 20, Main.colorizeWithoutPrefix("&cCANCELLED"), Main.colorizeWithoutPrefix("&cYour game was stopped by a moderator."));
                    player.sendMessage(Main.colorize("&cYour game was cancelled by a moderator. Type &f/join " + game.getName() + " " + map.name + " &cto rejoin the game."));
                    Wrapper.teleportToSpawn(player);
                    Wrapper.clearInventory(player.getInventory());
                }
                map.queue.resetVariables();
            }
        }
        sender.sendMessage(Main.colorize("&aAll current games were stopped and reset. Players, people in queue, and spectators were notified."));
        return true;
    }
}
