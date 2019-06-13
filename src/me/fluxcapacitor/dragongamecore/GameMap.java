package me.fluxcapacitor.dragongamecore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class GameMap {
    public final String name;
    public final Queue queue;
    private final ArrayList<GameBlock> blocks = new ArrayList<>();
    private final DragonGame game;
    private final ArrayList<Coordinate> spawnPoints;
    //private ArrayList<Team> teams;
    //private ArrayList<Coordinate> tempSpawnPoints;

    public GameMap(DragonGame game, String name, int countdownTime, int maxPlayers, int startRequirement) {
        this.name = name;
        this.game = game;
        this.spawnPoints = new ArrayList<>();
        //this.tempSpawnPoints = new ArrayList<>();
        this.queue = new Queue(game, this, countdownTime, maxPlayers, startRequirement);
        Debug.verbose("New map was registered: " + this.toString());
    }

    @Override
    public String toString() {
        String worldName;
        try {
            worldName = this.queue.getWorldName();
        } catch (NullPointerException | IndexOutOfBoundsException ignored) {
            worldName = "[Unknown]";
        }
        return this.getClass().getCanonicalName() + ":"
                + "\n    Game: " + game.getName()
                + "\n    Spawn points: " + this.spawnPoints.toString()
                + "\n    World name: " + worldName
                + "\n    Queue: " + this.queue.toString();
    }

    /*
    THE STRING REPRESENTATION OF A BLOCK:
    x;y;z;material;data;worldName
    (6 parts)
    */
    void addBlock(String blockRepresentation) {
        String[] parts = blockRepresentation.split(";");
        Coordinate coordinate = new Coordinate(parts[0], parts[1], parts[2]);
        String[] mat = parts[3].split(":");
        Material material = Material.getMaterial(mat[0]);
        String worldName = parts[4];
        if (material == null) {
            if (mat[0].equals("TEAM_SPAWNPOINT")) {
                //We got a team spawn point!
                //Find what team it's for & add the coordinate to the Team object.
                for (Team t : getTeams()) {
                    if (ChatColor.stripColor(t.getTeamName()).equals(ChatColor.stripColor(mat[1]))) {
                        //We found a matching team!
                        t.setSpawnPoint(coordinate.asLocation(worldName));
                    }
                }
                material = Material.AIR;
            }
        }
        byte data;
        if (mat.length >= 2) {
            try {
                data = Byte.parseByte(mat[1]);
            } catch (NumberFormatException exception) {
                data = 0;
            }
        } else data = 0;


        if ((!this.game.isIgnoreAir()) | (this.game.isIgnoreAir() && !Objects.equals(material, Material.AIR))) {
            if (!game.isFFA() && material == null && parts[3].equalsIgnoreCase("TEAM_SPAWN")) {
                material = Material.AIR;
            } else {
                this.addBlock(coordinate, material, worldName, data);
            }
        }

        //This is a spawn point of the map! All players will teleport to here or another spawnpoint
        // when the pregame starts.
        if (game.isFFA() & (Objects.equals(material, Material.MONSTER_EGG) | material.equals(Material.MONSTER_EGGS))) {
            this.spawnPoints.add(coordinate);
        }
    }

    private void addBlock(Coordinate coordinate, Material block, String worldName, byte data) {
        this.blocks.add(new GameBlock(coordinate, block, worldName, data));
    }

    @SuppressWarnings("deprecation")
    public void reset() {
        //Reset the map by looping over every saved block and resetting it to what was in the maps file
        for (GameBlock block : this.blocks) {
            //Get info about the individual block
            Coordinate coordinate = block.getCoordinate();
            int x = coordinate.getX();
            int y = coordinate.getY();
            int z = coordinate.getZ();
            String worldName = block.getWorldName();
            String blockType = block.getBlock();
            String blockData = block.getData();
            Material mat = Material.getMaterial(blockType);
            //If it's the spawn point, place air instead
            if (mat.equals(Material.MONSTER_EGG) || mat.equals(Material.MONSTER_EGGS) || blockType.equals("TEAM_SPAWNPOINT"))
                mat = Material.AIR;
            byte data;
            try {
                data = Byte.parseByte(blockData);
            } catch (NumberFormatException exception) {
                data = 0;
            }

            //Set the block
            Bukkit.getWorld(worldName).getBlockAt(x, y, z).setType(mat);
            Bukkit.getWorld(worldName).getBlockAt(x, y, z).setData(data);
        }
        //Reset the teams if it's a team game.
        if (!game.isFFA()) {
            this.queue.teams = game.getTeams();
        }
    }

    public ArrayList<GameBlock> getBlocks() {
        return this.blocks;
    }

    @Deprecated
    public Location getSpawnPoint() {
        Coordinate point = this.spawnPoints.get(0);
        Location location = point.asLocation(this.queue.getWorldName());
        return location.add(0.5, 0, 0.5);
    }

    public Location getSpawnPoint(Player player) {
        return getSpawnPoint(game.getWrapper().getTeam(player));
    }

    public Location getSpawnPoint(Team team) {
        if (team == null) {
            try {
                Coordinate point = this.spawnPoints.get(new Random().nextInt(this.spawnPoints.size()));
                Location location = point.asLocation(this.queue.getWorldName());
                return location.add(0.5, 0, 0.5);
            } catch (NullPointerException | IllegalArgumentException | IndexOutOfBoundsException ignored) {
                return game.getTeams().get(0).getSpawnPoint();
            }
        } else {
            if (game.isFFA()) {
                Coordinate point = this.spawnPoints.get(new Random().nextInt(this.spawnPoints.size()));
                Location location = point.asLocation(this.queue.getWorldName());
                return location.add(0.5, 0, 0.5);
            } else {
                //This is a team game. Get the team's spawn point.
                for (Team t : this.getTeams()) {
                    if (t.getTeamName().equals(team.getTeamName())) {
                        //We found a match!
                        return t.getSpawnPoint();
                    }
                }
            }
        }
        return null;
    }

    public DragonGame getGame() {
        return game;
    }

    public ArrayList<Team> getTeams() {
        return this.queue.getTeams();
    }
}
