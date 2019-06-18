package me.fluxcapacitor.dragongamecore;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * A simple coordinate class for representing locations.
 */
public class Coordinate {
    private final int z;
    private final int x;
    private final int y;

    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coordinate(String x, String y, String z) {
        this.x = Integer.parseInt(x);
        this.y = Integer.parseInt(y);
        this.z = Integer.parseInt(z);
    }

    public Coordinate(Location loc) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String toString() {
        return this.getClass().getCanonicalName() + "[x=" + x + ",y=" + y + ",z=" + z + "]";
    }

    public Location asLocation(String world) {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
