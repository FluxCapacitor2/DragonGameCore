package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.GameMap;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ListMapsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            DragonGame game = Wrapper.findGame(args[0]);
            if (game != null) {
                sender.sendMessage(Main.colorize("&bAll maps available for &f" + game.getName() + "&b:"));
                for (GameMap map : game.getWrapper().maps) {
                    sender.sendMessage(Main.colorize("    &b" + map.name));
                }
                return true;
            }
        }
        return false;
    }
}
