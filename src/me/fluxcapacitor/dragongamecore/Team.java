package me.fluxcapacitor.dragongamecore;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * A team, which is map-specific.
 *
 * @author FluxCapacitor
 * @see GameMap
 * @see DragonGame
 */
public class Team {

    public org.bukkit.scoreboard.Team team;
    ArrayList<Player> players;
    private String teamName;
    private Location spawnPoint;
    private ItemStack material;

    @SuppressWarnings("unused")
    public Team() {
        this.teamName = "[Unnamed Team]";
        this.players = new ArrayList<>();
    }

    public Team(Player player) {
        this.setPlayer(player);
        this.setTeamName(player.getDisplayName());
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    /*
    public Team setPlayers(ArrayList<Player> players) {
        this.players = players;
        return this;
    }
     */

    public String getTeamName() {
        return teamName;
    }

    @SuppressWarnings("WeakerAccess")
    public void setTeamName(String teamName) {
        this.teamName = Main.colorizeWithoutPrefix(teamName);
    }

    private void setPlayer(Player player) {
        this.players = new ArrayList<>();
        this.players.add(player);
    }

    void addPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public String toString() {
        return this.getClass().getCanonicalName() + "[name='" + this.getTeamName() + "',players='" + this.getPlayers().toString() + "']";
    }

    boolean matches(ArrayList<Team> teams) {
        for (Team t : teams) {
            if (t.getTeamName().equals(this.getTeamName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public void removeFriendlyFire(DragonGame game) {
        if (!game.isFFA()) {
            if (Main.scoreboard.getTeam(this.getTeamName()) != null) {
                Main.scoreboard.getTeam(this.getTeamName()).unregister();
            }
            this.team = Main.scoreboard.registerNewTeam(this.getTeamName());
            for (Player p : this.getPlayers()) {
                team.addPlayer(p);
            }
            team.setAllowFriendlyFire(false);
        }
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location location) {
        Debug.verbose("Set spawn point of " + getTeamName() + " to " + location.toString());
        this.spawnPoint = location;
    }

    public ItemStack getMaterial() {
        return material;
    }

    public void setMaterial(ItemStack material) {
        this.material = material;
    }
}
