package me.fluxcapacitor.dragongamecore;

import me.skater77i.bluedragon.debug.DebugAPI;

/**
 * A simple class for debugging that relies on skater77i's DebugAPI
 */
public class Debug {

    public static void verbose(String string) {
        DebugAPI.log(string, 0);
    }

    public static void info(String string) {
        DebugAPI.log(string, 1);
    }

    public static void warn(String string) {
        DebugAPI.log(string, 2);
    }

    public static void error(String string) {
        DebugAPI.log(string, 3);
    }
}
