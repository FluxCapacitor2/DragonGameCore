package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class ReloadCommand implements CommandExecutor {
    static void reloadConfig(DragonGame game) {
        game.loadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<DragonGame> games = new ArrayList<>();
        if (args.length > 0) {
            games.add(Wrapper.findGame(args[0]));
        } else {
            games.addAll(Main.games);
        }
        for (DragonGame game : games) {
            reloadConfig(game);
        }
        sender.sendMessage(Main.colorize("&aConfig reloaded!"));
        return true;
    }
}
