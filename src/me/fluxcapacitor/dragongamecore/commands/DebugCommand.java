package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.party.Party;
import me.fluxcapacitor.dragongamecore.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Bukkit.getConsoleSender().sendMessage("PARTIES:");
        for (Party party : PartyManager.parties) {
            Bukkit.getConsoleSender().sendMessage("    " + party.toString());
        }
        for (DragonGame game : Main.games) {
            Bukkit.getConsoleSender().sendMessage("=============== [GAME DUMP: " + game.getName() + "] ===============");
            game.getWrapper().dumpMaps();
            Bukkit.getConsoleSender().sendMessage("=============== [/GAME DUMP: " + game.getName() + "] ===============");
        }
        sender.sendMessage(Main.colorize("&bDebug info was dumped to the console."));
        return true;
    }
}
