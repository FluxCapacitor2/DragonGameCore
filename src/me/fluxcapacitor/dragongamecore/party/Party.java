package me.fluxcapacitor.dragongamecore.party;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Party {

    public ArrayList<Player> players;
    public Player leader;
    public boolean isPublic;
    public boolean isPrivate;

    public Party() {
        this.players = new ArrayList<>();
    }

    public Party(Player player) {
        this.players = new ArrayList<>();
        this.leader = player;
        this.players.add(player);
        this.isPublic = false;
        this.isPrivate = false;
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        this.sendMessage("&f" + player.getDisplayName() + "&a has left the party.");
        if (this.players.size() == 1) {
            if (PartyManager.getRequestAmount(this) == 0) {
                this.sendMessage("&cThe party has been disbanded because there is not enough players and all invites have expired.");
                this.disband();
            }
        }
    }

    public void disband() {
        for (PartyInvite i : PartyManager.invites) {
            if (this.players.contains(i.from) | i.party.equals(this)) {
                PartyManager.invites.remove(i);
            }
        }
        this.players = new ArrayList<>();
        this.leader = null;
        PartyManager.parties.remove(this);
    }

    public String toString() {
        return getClass().getCanonicalName() + "[players=" + players.toString() + ",leader=" + leader.getName() + ",invites=" + PartyManager.findInvites(this) + "]";
    }

    public void sendMessage(String string) {
        for (Player p : this.players) {
            p.sendMessage(PartyManager.colorize(string));
        }
    }
}
