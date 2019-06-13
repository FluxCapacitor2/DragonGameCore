package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.GameMap;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            DragonGame game = Wrapper.findGame(args[0]);
            if (game != null) {
                Wrapper wrapper = game.getWrapper();
                Player player = (Player) sender;
                GameMap map;
                if (args.length >= 2) {
                    //They specified a map, let's give it to 'em
                    map = wrapper.findMap(args[0]);
                } else {
                    //They aren't picky, just give them the closest one to starting (the most filled one)
                    map = wrapper.findMostPopulatedMap();
                }
                map.queue.add(player);
                wrapper.updatePlayerCounts();
                //sender.sendMessage(Main.colorize("&aYou have queued for spleef on the map &f" + map.getName() + "&a."));
                return true;
            } else {
                sender.sendMessage(Main.colorize("&cThere is no game called '" + args[0] + "' on this server!"));
                return true;
            }
        }
        return false;
    }
}
