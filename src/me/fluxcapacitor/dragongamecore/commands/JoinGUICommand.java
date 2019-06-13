package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class JoinGUICommand implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            DragonGame game = Wrapper.findGame(args[0]);
            if (game != null) {
                Main.instance.guiManager.joinInv.open(game, (Player) sender);
                return true;
            } else return false;
        } else return false;
    }
}
