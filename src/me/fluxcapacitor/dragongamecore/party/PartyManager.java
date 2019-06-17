package me.fluxcapacitor.dragongamecore.party;

import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PartyManager {

    public static ArrayList<Party> parties = new ArrayList<>();
    public static ArrayList<PartyInvite> invites = new ArrayList<>();

    public static Party findParty(Player player) {
        for (Party p : parties) {
            if (p.players.contains(player)) return p;
        }
        return null;
    }

    public static void sendRequest(Player to, Player from, Party party) {
        invites.add(new PartyInvite(to, from, party));
    }

    public static ArrayList<PartyInvite> findInvites(Party party) {
        ArrayList<PartyInvite> matches = new ArrayList<>();
        for (PartyInvite inv : invites) {
            if (inv.party.leader != null && party.leader != null && inv.party.leader.getUniqueId().equals(party.leader.getUniqueId())) {
                matches.add(inv);
            }
        }
        return matches;
    }

    public static ArrayList<PartyInvite> findInvites(Party party, Player player) {
        ArrayList<PartyInvite> invites = findInvites(party);
        ArrayList<PartyInvite> matches = new ArrayList<>();
        for (PartyInvite req : invites) {
            if (req.to.getUniqueId().equals(player.getUniqueId())) {
                matches.add(req);
            }
        }
        return matches;
    }

    public static int getRequestAmount(Party party) {
        return findInvites(party).size();
    }

    public static String colorize(String string) {
        return Main.colorizeWithoutPrefix("&dParty &8>> &7" + string);
    }

    public static boolean isInParty(Player player) {
        return findParty(player) != null;
    }

    public static boolean isLeader(Player player) {
        for (Party party : parties) {
            if (party.leader.equals(player)) return true;
        }
        return false;
    }
}
