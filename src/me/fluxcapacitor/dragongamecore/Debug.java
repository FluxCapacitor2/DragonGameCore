package me.fluxcapacitor.dragongamecore;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class Debug {

    public static final ArrayList<CommandSender> subscribed = new ArrayList<>();

    public static void verbose(String string) {
        sendToSubscribed("&d&lVERBOSE&r " + string);
    }

    public static void info(String string) {
        sendToSubscribed("&b&lINFO&r " + string);
    }

    public static void warn(String string) {
        sendToSubscribed("&e&lWARNING&r " + string);
    }

    public static void error(String string) {
        sendToSubscribed("&4&lERROR&r " + string);
    }

    private static void sendToSubscribed(String message) {
        for (CommandSender sender : subscribed) {
            sender.sendMessage(Main.colorize(message));
        }
    }
}
