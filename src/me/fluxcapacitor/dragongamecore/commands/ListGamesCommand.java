package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListGamesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Main.instance.guiManager.gamesInv.open(Main.games.get(0), player);
        } else {
            sender.sendMessage(Main.colorize("&bAll loaded games on this server:"));
            for (DragonGame game : Main.games) {
                sender.sendMessage(Main.colorize("    &b" + game.getName()));
            }
        }
        return true;
    }
}
