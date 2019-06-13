package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ListGamesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(Main.colorize("&bHere are all the games you can play:"));
        for (DragonGame game : Main.games) {
            sender.sendMessage(Main.colorize("    &b" + game.getName()));
        }
        return true;
    }
}
