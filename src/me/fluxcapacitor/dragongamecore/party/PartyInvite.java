package me.fluxcapacitor.dragongamecore.party;

import me.fluxcapacitor.dragongamecore.CountdownTimer;
import me.fluxcapacitor.dragongamecore.Debug;
import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.entity.Player;

public class PartyInvite {

    public Party party;
    public CountdownTimer timer;
    public Player to;
    public Player from;

    public PartyInvite(Player to, Player from, Party party) {
        if (PartyManager.findParty(to) != null) {
            //They're already in a party
            from.sendMessage(PartyManager.colorize("&f" + to.getDisplayName() + "&c is already in a party!"));
        } else {
            this.to = to;
            this.from = from;
            this.party = party;
            this.timer = new CountdownTimer(Main.instance, 60, this::beforeTimer, this::afterTimer, this::everySecond);
            this.to.sendMessage("");
            this.to.sendMessage(PartyManager.colorize(from.getDisplayName() + " &einvited you to their party. To join " +
                    "the party, use &7/party join " + from.getName() + "&e."));
            this.to.sendMessage("");
            this.timer.scheduleTimer();
            this.from.sendMessage(PartyManager.colorize("&aInvite sent."));
        }
    }

    private void afterTimer() {
        Debug.verbose("Party invite to " + this.to.getName() + " from " + this.from.getName() + " expired after 60 seconds.");
        PartyManager.invites.remove(this);
    }

    public void accept() {
        Debug.verbose("Trying to accept invite...");
        if (PartyManager.invites.contains(this)) {
            Debug.verbose("The invite exists! Joining...");
            PartyManager.invites.remove(this);
            this.timer.cancelTimer();
            for (Player p : this.party.players) {
                p.sendMessage(PartyManager.colorize("&f" + to.getDisplayName() + "&a has joined the party!"));
            }
            this.party.players.add(this.to);
            to.sendMessage(PartyManager.colorize("&aYou have joined the party!"));
        } else {
            to.sendMessage(PartyManager.colorize("&cThe invite has expired. Ask the party leader to invite you again."));
        }

    }

    private void beforeTimer() {
    }

    private void everySecond(CountdownTimer countdownTimer) {
    }
}
