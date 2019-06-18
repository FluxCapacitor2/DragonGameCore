package me.fluxcapacitor.dragongamecore;

import org.bukkit.Material;

/**
 * A block which is part of a GameMap.
 *
 * @author FluxCapacitor
 * @see GameMap
 * @see DragonGame
 */
public class GameBlock {
    private final Coordinate coordinate;
    private final String block;
    private final String worldName;
    private final String data;

    public GameBlock(Coordinate coordinate, Material block, String worldName, byte data) {
        this.coordinate = coordinate;
        this.block = block.toString();
        this.worldName = worldName;
        this.data = String.valueOf(data);
    }

    public GameBlock(Coordinate coordinate, String block, String worldName, String data) {
        this.coordinate = coordinate;
        this.block = block;
        this.worldName = worldName;
        this.data = data;
    }

    /**
     * Get the String representation of the block
     *
     * @see GameMap#addBlock(String)
     * @return The string representation of this GameBlock
     */
    @SuppressWarnings("deprecation")
    public String toStringRepresentation() {
        return String.join(";", new String[] {
                coordinate.getX() + "",
                coordinate.getY() + "",
                coordinate.getZ() + "",
                block + ":" + data,
                worldName
        });
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getBlock() {
        return block;
    }

    public String getWorldName() {
        return worldName;
    }

    public String toString() {
        return this.getClass().getCanonicalName() + "[coordinate=" + coordinate.toString() + ",block=" + block + ",worldName=" + worldName + "]";
    }

    String getData() {
        return data;
    }
}
