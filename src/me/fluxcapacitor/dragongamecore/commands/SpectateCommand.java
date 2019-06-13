package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            Player player = (Player) sender;
            if (args.length >= 1) {
                DragonGame game = Wrapper.findGame(player);
                //noinspection ConstantConditions
                if (game == null | !game.getWrapper().isSpectating(player)) {
                    sender.sendMessage(Main.colorize("&cYou must be spectating in a game to use this command!"));
                    return true;
                }
                Player toSpectate = Bukkit.getPlayer(args[0]);
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(toSpectate.getLocation());
                return true;
            } else return false;
        } catch (NullPointerException ignored) {
            sender.sendMessage(Main.colorize("&cYou must be spectating in a game to use this command!"));
            return true;
        }
    }
}
