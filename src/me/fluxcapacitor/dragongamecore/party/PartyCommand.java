package me.fluxcapacitor.dragongamecore.party;

import me.fluxcapacitor.dragongamecore.Debug;
import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static me.fluxcapacitor.dragongamecore.party.PartyManager.findParty;
import static me.fluxcapacitor.dragongamecore.party.PartyManager.parties;

public class PartyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if (sender instanceof Player) {
                Player pSender = (Player) sender;

                if (args[0].equalsIgnoreCase("invite") | args[0].equalsIgnoreCase("i") |
                        args[0].equalsIgnoreCase("add") | Bukkit.getPlayer(args[0]) != null) {
                    //Invite player to the party
                    Player player = null;
                    if (args.length >= 2) {
                        player = Bukkit.getPlayer(args[1]);
                    } else if (Bukkit.getPlayer(args[0]) != null) {
                        player = Bukkit.getPlayer(args[0]);
                    }
                    if (player == null) {
                        sender.sendMessage(PartyManager.colorize("&cCould not find that player! Make sure they are online and their username is typed correctly."));
                    } else {
                        //We found the player that they specified. Send them a request.
                        Party party;
                        if (findParty(pSender) == null) {
                            party = new Party(pSender);
                            parties.add(party);
                        } else {
                            //They already have a party. Just add them without making a new one.
                            party = findParty(pSender);
                        }
                        assert party != null;
                        if (!party.players.contains(player)) {
                            PartyManager.sendRequest(player, pSender, party);
                        } else {
                            pSender.sendMessage(PartyManager.colorize("&cThat player is already in your party!"));
                        }
                    }

                } else if (args[0].equalsIgnoreCase("leave")) {
                    //Leave the party
                    Party party = findParty(pSender);
                    if (party != null) {
                        party.removePlayer(pSender);
                        pSender.sendMessage(PartyManager.colorize("&aYou have left the party."));
                    } else {
                        pSender.sendMessage(PartyManager.colorize("&cYou are not in a party!"));
                    }
                } else if (args[0].equalsIgnoreCase("join") | args[0].equalsIgnoreCase("j")) {
                    Debug.verbose(sender.getName() + " tried to join a party with /p join.");
                    //Join a party by player name
                    Player toJoin = Bukkit.getPlayer(args[1]);
                    if (toJoin != null) {
                        Debug.verbose("The player is not null.");
                        Party party = PartyManager.findParty(toJoin);
                        if (party != null) {
                            if (party.isPublic) {
                                Debug.verbose("The party is public.");
                                for (Player p : party.players) {
                                    p.sendMessage(PartyManager.colorize("&f" + pSender.getDisplayName() + "&a has joined the party!"));
                                }
                                party.players.add(pSender);
                                pSender.sendMessage(PartyManager.colorize("&aYou have joined the party!"));
                            } else {
                                Debug.verbose("The party is not null.");
                                ArrayList<PartyInvite> invites = PartyManager.findInvites(party, pSender);
                                if (invites.size() > 0) {
                                    invites.get(0).accept();
                                    Debug.verbose("The invite was accepted");
                                } else {
                                    sender.sendMessage(PartyManager.colorize("&cYou were not invited to this party!"));
                                }
                            }
                        } else {
                            sender.sendMessage(PartyManager.colorize("&f" + toJoin.getDisplayName() + "&c does not have a party for you to join!"));
                        }
                    } else {
                        sender.sendMessage(PartyManager.colorize("&cCould not find that player! Make sure they are online and their username is typed correctly."));
                    }
                } else if (args[0].equalsIgnoreCase("list") | args[0].equalsIgnoreCase("l")) {
                    //List party members
                    Party party = PartyManager.findParty(pSender);
                    if (party != null) {
                        ArrayList<String> players = new ArrayList<>();
                        party.players.forEach(p -> players.add(p.getDisplayName()));
                        sender.sendMessage(PartyManager.colorize("&aParty members (" + players.size() + "): &7" + String.join("&a, &7", players)));
                    } else {
                        sender.sendMessage(PartyManager.colorize("&cYou are not in a party!"));
                    }
                } else if (args[0].equalsIgnoreCase("disband")) {
                    Party party = PartyManager.findParty(pSender);
                    if (party != null) {
                        party.disband();
                    }
                } else if (args[0].equalsIgnoreCase("promote")) {
                    Party party = PartyManager.findParty(pSender);
                    if (party != null) {
                        if (party.leader.getUniqueId().equals(pSender.getUniqueId())) {
                            if (args.length >= 2) {
                                Player player = Bukkit.getPlayer(args[1]);
                                party.leader = player;
                                if (player != null) {
                                    party.sendMessage("&f" + player.getDisplayName() + " &awas promoted to party leader.");
                                    party.leader.sendMessage(PartyManager.colorize("&aYou were promoted to the party leader."));
                                } else {
                                    sender.sendMessage(PartyManager.colorize("&cCould not find that player! " +
                                            "Make sure they are online and their username is typed correctly."));
                                }
                            }
                        } else {
                            sender.sendMessage(PartyManager.colorize("&cYou must be the party leader to use this command!"));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("chat") | args[0].equalsIgnoreCase("c")) {
                    if (args.length >= 2) {
                        Party party = PartyManager.findParty(pSender);
                        StringBuilder msg = new StringBuilder(args[1]);
                        if (args.length > 2) {
                            for (int i = 2; i < args.length; i++) {
                                msg.append(" ").append(args[i]);
                            }
                        }
                        if (party != null) {
                            for (Player p : party.players) {
                                p.sendMessage(PartyManager.colorize(pSender.getDisplayName() + "&8: &f") + msg);
                            }
                        } else {
                            sender.sendMessage(PartyManager.colorize("&cYou are not in a party!"));
                        }
                    } else {
                        sender.sendMessage(PartyManager.colorize("&cYou must specify a message."));
                    }
                } else if (args[0].equalsIgnoreCase("kick")) {
                    if (args.length >= 2) {
                        Party party = PartyManager.findParty(pSender);
                        if (party != null) {
                            if (party.leader.getUniqueId().equals(pSender.getUniqueId())) {
                                Player toKick = Bukkit.getPlayer(args[1]);
                                if (toKick != null) {
                                    if (!party.players.remove(toKick)) {
                                        sender.sendMessage(PartyManager.colorize("&cThat player is not in your party!"));
                                    } else {
                                        party.sendMessage("&f" + toKick.getDisplayName() + " &ewas removed from the party by &f" + pSender.getDisplayName() + "&e.");
                                    }
                                } else {
                                    sender.sendMessage(PartyManager.colorize("&cCould not find that player! " +
                                            "Make sure they are online and their username is typed correctly."));
                                }
                            } else {
                                sender.sendMessage(PartyManager.colorize("&cYou must be the party leader to use this command!"));
                            }
                        }
                    } else {
                        sender.sendMessage(PartyManager.colorize("&cYou must specify a player."));
                    }
                } else if (args[0].equalsIgnoreCase("public")) {
                    Party party = PartyManager.findParty(pSender);
                    if (party != null) {
                        if (pSender.equals(party.leader)) {
                            party.isPublic = true;
                            for (Player p : party.players) {
                                p.sendMessage(PartyManager.colorize("&aThe party leader has made the party public! Anyone can join using &7/party join " + party.leader.getName() + "&a."));
                            }
                        } else {
                            sender.sendMessage(PartyManager.colorize("&cYou must be the party leader to use this command!"));
                        }
                    } else {
                        sender.sendMessage(PartyManager.colorize("&cYou are not in a party!"));
                    }
                } else if (args[0].equalsIgnoreCase("private")) {
                    Party party = PartyManager.findParty(pSender);
                    if (party != null) {
                        if (pSender.equals(party.leader)) {
                            party.isPrivate = !party.isPrivate;
                            for (Player p : party.players) {
                                if (party.isPrivate) p.sendMessage(PartyManager.colorize("&aThe party leader has " +
                                        "turned on &fPrivate Games&a!"));
                                else p.sendMessage(PartyManager.colorize("&aThe party leader has " +
                                        "turned off &fPrivate Games&a."));
                            }
                        } else {
                            sender.sendMessage(PartyManager.colorize("&cYou must be the party leader to use this command!"));
                        }
                    } else {
                        sender.sendMessage(PartyManager.colorize("&cYou are not in a party!"));
                    }
                } else {
                    sender.sendMessage(Main.colorize("&aParty Commands:\n" +
                            "&e/party help &7- &bShow this help message.\n" +
                            "&e/party invite <player> &7- &bInvite &e<player>&b to your party.\n" +
                            "&e/party kick <player> &7- &bKick &e<player>&b from your party.\n" +
                            "&e/party disband &7- &bDisband your party and kick all members.\n" +
                            "&e/party promote <player> &7- &bMake &e<player>&b the party leader.\n" +
                            "&e/party leave &7- &bLeave the party.\n" +
                            "&e/party list &7- &bList everyone in the party.\n" +
                            "&e/party chat <message> &7- &bSend &e<message>&b to all party members.\n" +
                            "&e/party public &7- &bAllow everyone to join the party. &f(&aPREMIUM&f ONLY)\n" +
                            "&e/party public &7- &bToggle private games &f(&aPREMIUM&f ONLY)"));
                    return true;
                }
            }
        } else return false;
        return true;
    }
}
