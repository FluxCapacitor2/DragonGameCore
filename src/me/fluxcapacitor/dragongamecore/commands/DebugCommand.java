package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.Debug;
import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length > 0) {
            if (args[0].equals("toggle")) {
                if (Debug.subscribed.contains(sender))
                    args[0] = "off";
                else
                    args[0] = "on";
            }
            if (args[0].equals("start") | args[0].equals("on")) {
                //Enable debugging for them if they have permission
                if ((sender instanceof Player && sender.hasPermission("arcade.admin.debug")) || sender instanceof ConsoleCommandSender) {
                    //They do have permission! "Subscribe" them to debug updates.
                    if (!Debug.subscribed.contains(sender)) {
                        Debug.subscribed.add(sender);
                        sender.sendMessage(Main.colorize("&aYou will now receive debug messages."));
                    } else sender.sendMessage(Main.colorize("&cYou are already recieving debug messages!"));
                }
            } else if (args[0].equals("stop") | args[0].equals("off")) {
                if ((sender instanceof Player && sender.hasPermission("arcade.admin.debug")) || sender instanceof ConsoleCommandSender) {
                    //They do have permission! "Subscribe" them to debug updates.
                    if (Debug.subscribed.contains(sender)) {
                        Debug.subscribed.remove(sender);
                        sender.sendMessage(Main.colorize("&aYou have stopped recieving debug messages."));
                    } else sender.sendMessage(Main.colorize("&cYou are not currently recieving debug messages!"));
                }
            }
        } else {
            for (DragonGame game : Main.games) {
                Bukkit.getConsoleSender().sendMessage("=============== [GAME DUMP: " + game.getName() + "] ===============");
                game.getWrapper().dumpMaps();
                Bukkit.getConsoleSender().sendMessage("=============== [/GAME DUMP: " + game.getName() + "] ===============");
            }
            sender.sendMessage(Main.colorize("&bDebug info was dumped to the console."));
        }
        return true;
    }
}
