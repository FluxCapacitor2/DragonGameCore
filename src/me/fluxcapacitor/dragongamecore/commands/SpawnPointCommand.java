package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SpawnPointCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 3 && sender instanceof Player) {
            DragonGame game = Wrapper.findGame(args[0]);
            if (game != null && !game.isFFA()) {
                GameMap map = game.getWrapper().findMap(args[1]);
                if (map != null) {
                    for (Team t : map.getTeams()) {
                        //Debug.verbose("Finding team: Comparing " + ChatColor.stripColor(t.getTeamName()) + " to " + args[2]);
                        if (ChatColor.stripColor(t.getTeamName()).equalsIgnoreCase(args[2])) {
                            //Debug.verbose("Match found! Adding spawn point...");
                            //We found a match!
                            //This team matches team name, map, and game.
                            //Let's add the spawn point for THIS team & stop the loop.
                            Player player = (Player) sender;
                            Location location = player.getLocation();
                            FileConfiguration config = game.getConfigObject();
                            File mapsFile = game.getMapsFile();
                            try {
                                config.load(mapsFile);
                                Debug.info("Added spawn point to map " + map.name);
                            } catch (IOException | InvalidConfigurationException e) {
                                sender.sendMessage(Main.colorize("&cThere was a problem loading the maps file. The full error message has been printed in the console."));
                                Debug.warn("There was a problem loading `" + game.getMapsFile().getName() + "`. The error message has been printed below.");
                                Debug.warn(e.getMessage());
                                e.printStackTrace();
                            }
                            Debug.verbose("Gathering blocks in map from maps." + map.name + " in " + game.getMapsFile().getName() + "...");
                            List<String> currentBlocks = config.getStringList("maps." + map.name);
                            if (currentBlocks.size() > 0) { //To prevent a possible mishap of deleting the entire map
                                currentBlocks.add(
                                        new GameBlock(new Coordinate(location), "TEAM_SPAWNPOINT", player.getWorld().getName(), t.getTeamName()
                                        ).toStringRepresentation());
                                config.set("maps." + map.name, currentBlocks);
                                try {
                                    config.save(game.getMapsFile());
                                    Debug.info("Added spawn point to map " + map.name);
                                    sender.sendMessage(Main.colorize("&bAdded spawn point to &f" + game.getName() + "&b on &f" + map.name + "&b!"));
                                    return true;
                                } catch (IOException e) {
                                    sender.sendMessage(Main.colorize("&cThere was a problem saving the maps file. The full error message has been printed in the console."));
                                    Debug.warn("There was a problem saving `" + game.getMapsFile().getName() + "`. The error message has been printed below.");
                                    Debug.warn(e.getMessage());
                                    e.printStackTrace();
                                }
                            } else {
                                sender.sendMessage(Main.colorize("&cThere was a problem adding the spawn point...\nCheck the console for more details."));
                                Debug.warn("While attempting to add a spawn point for " + map.name + " in " +
                                        game.getName() + ", there were no loaded blocks in the map. The overwrite was stopped " +
                                        "to prevent deleting the entire map except for this new point.");
                            }
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }
}
