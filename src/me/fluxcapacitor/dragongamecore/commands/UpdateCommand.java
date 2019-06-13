package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class UpdateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<DragonGame> games = new ArrayList<>();
        if (args.length > 0 && Wrapper.findGame(args[0]) != null) {
            games.add(Wrapper.findGame(args[0]));
        } else {
            games.addAll(Main.games);
        }
        for (DragonGame game : games) {
            Wrapper wrapper = game.getWrapper();
            wrapper.updatePlayerCounts();
            wrapper.cancelGamesIfUnderPlayerCount();
        }
        sender.sendMessage(Main.colorize("&aGame player counts were updated, offline players were removed, and " +
                "games with a lacking amount of players have been cancelled."));
        return true;
    }
}
