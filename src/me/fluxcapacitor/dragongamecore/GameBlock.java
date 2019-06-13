package me.fluxcapacitor.dragongamecore;

import org.bukkit.Material;

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
     * THE STRING REPRESENTATION OF A BLOCK
     *
     * @see GameMap#addBlock(String)
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
