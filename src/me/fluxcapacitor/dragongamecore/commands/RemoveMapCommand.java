package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

public class RemoveMapCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 2) {
            DragonGame game = Wrapper.findGame(args[0]);
            if (game != null) {
                GameMap map = game.getWrapper().findMap(args[1]);
                if (map != null) {
                    String name = map.name;
                    try {
                        FileConfiguration config = Main.instance.getConfig();
                        config.load(game.getMapsFile());
                        config.set("maps." + name, null);
                        config.save(game.getMapsFile());
                        return true;
                    } catch (IOException | InvalidConfigurationException e) {
                        Debug.warn("There was an error saving or loading the config file!");
                        Debug.warn("The full error is printed below.");
                        Debug.warn(e.getMessage());
                        e.printStackTrace();

                        sender.sendMessage(Main.colorize("&eThere was an error saving or loading the config file!"));
                        sender.sendMessage(Main.colorize("&eCheck the console for more details."));
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
