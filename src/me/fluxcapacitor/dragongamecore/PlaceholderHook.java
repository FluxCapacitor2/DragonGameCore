package me.fluxcapacitor.dragongamecore;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * The class that implements PlaceholderAPI integration.
 *
 * @author FluxCapacitor
 * @see me.clip.placeholderapi.expansion.PlaceholderExpansion
 */
class PlaceholderHook extends PlaceholderExpansion {

    private final DragonGame game;

    PlaceholderHook(DragonGame game) {
        this.game = game;
    }

    @Override
    public String getName() {
        return Main.instance.getDescription().getName();
    }

    @Override
    public String getIdentifier() {
        return "arcade." + game.getName();
    }

    @Override
    public String getAuthor() {
        return "FluxCapacitor";
    }

    @Override
    public String getVersion() {
        return Main.instance.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer p, String id) {
        Wrapper wrapper = game.getWrapper();
        String returnValue = "%" + getIdentifier() + "_" + id + "%";

        for (GameMap map : wrapper.maps) {
            if (id.equalsIgnoreCase("status_" + map.name)) {
                //Get the status of the map and return it
                if (map.queue.getPlayerCount() > 0 && map.queue.timerStarted && !map.queue.gameStarted) {
                    returnValue = map.queue.getPlayerCount() + "/" + map.queue.MAX_PLAYERS_PER_LOBBY + " STARTING";
                } else if (map.queue.getPlayerCount() > 0 && map.queue.timerStarted) {
                    returnValue = map.queue.getPlayerCount() + "/" + map.queue.MAX_PLAYERS_PER_LOBBY + " INGAME";
                } else {
                    returnValue = map.queue.queue.size() + "/" + map.queue.MAX_PLAYERS_PER_LOBBY + " WAITING";
                }
            }
        }

        if (id.equalsIgnoreCase("total_ingame")) {
            int total = 0;
            for (GameMap map : wrapper.maps) {
                total += map.queue.getPlayerCount();
            }
            return total + " ingame";
        }

        if (id.equalsIgnoreCase("total_queued")) {
            int total = 0;
            for (GameMap map : wrapper.maps) {
                total += map.queue.queue.size();
            }
            return total + " queued";
        }

        if (id.equalsIgnoreCase("total_spectating")) {
            int total = 0;
            for (GameMap map : wrapper.maps) {
                total += map.queue.spectators.size();
            }
            return total + " spectating";
        }

        return Main.colorizeWithoutPrefix(returnValue);
    }
}
