package me.fluxcapacitor.dragongamecore.commands;

import me.fluxcapacitor.dragongamecore.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddMapCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //Command: /aaaddmap <game> <x1> <y1> <z1> <x2> <y2> <z2> <name>
        //Total: 8 arguments
        if (args.length >= 8) {
            DragonGame game = Wrapper.findGame(args[0]);
            World world;
            //Assign the world based on the player's current world or an argument (if you are a Player, it uses your current world)
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                world = Bukkit.getWorld(args[8]);
            }
            Coordinate bound1 = new Coordinate(args[1], args[2], args[3]);
            Coordinate bound2 = new Coordinate(args[4], args[5], args[6]);

            int minX = Math.min(bound1.getX(), bound2.getX());
            int minY = Math.min(bound1.getY(), bound2.getY());
            int minZ = Math.min(bound1.getZ(), bound2.getZ());

            int maxX = Math.max(bound1.getX(), bound2.getX());
            int maxY = Math.max(bound1.getY(), bound2.getY());
            int maxZ = Math.max(bound1.getZ(), bound2.getZ());

            String name = args[7];

            ArrayList<String> coordsList = new ArrayList<>();

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Location location = new Location(world, x, y, z);
                        @SuppressWarnings("deprecation")
                        String representation = new GameBlock(new Coordinate(x, y, z), location.getBlock().getType(), world.getName(), location.getBlock().getData()).toStringRepresentation();
                        coordsList.add(representation);
                    }
                }
            }
            assert game != null;
            ConfigurationSection mapsSection = game.getMapsSection();
            FileConfiguration cfg = game.getConfigObject();
            File mapsFile = game.getMapsFile();

            String addingMapMsg = Main.colorize("&aAdding new map &f" + name + "&a for &f" + game.getName() + "&a (&f" + coordsList.size() + "&a blocks)...");
            sender.sendMessage(addingMapMsg);
            Debug.info(addingMapMsg);

            try {
                cfg.load(mapsFile);
            } catch (IOException | InvalidConfigurationException e) {
                sender.sendMessage(Main.colorize("&cThere was a problem loading the maps file. The full error message has been printed in the console."));
                Debug.warn("There was a problem loading `maps.yml`. The error message has been printed below.");
                Debug.warn(e.getMessage());
                e.printStackTrace();
            }

            if (mapsSection == null) {
                try {
                    cfg.load(mapsFile);
                    cfg.createSection("maps");
                    cfg.save(mapsFile);
                } catch (IOException | InvalidConfigurationException e) {
                    sender.sendMessage(Main.colorize("&cThere was an error with loading and/or adding the `maps` section to `maps.yml`. The full error is detailed in the server console."));
                    Debug.warn("There was a problem loading/adding a section to `maps.yml`. The error message has been printed below.");
                    Debug.warn(e.getMessage());
                    e.printStackTrace();
                }
            }
            mapsSection = game.getMapsSection();

            cfg.set("maps." + name, coordsList);
            mapsSection.set(name, coordsList);
            try {
                cfg.save(mapsFile);
                sender.sendMessage(Main.colorize("&aNew map '" + name + "' was added!"));
                if (!game.isFFA()) sender.sendMessage(Main.colorize("&b&lMake sure you add spawn points for each " +
                        "of the teams to your map. Without adding these, the map will NOT work! This can be done by using " +
                        "&r&f/aaspawnpoint <game name> <map name> <team name>&b&l, which sets a team's spawn point " +
                        "at your current location."));
                return true;
            } catch (IOException e) {
                sender.sendMessage(Main.colorize("&cThere was a problem saving the maps file. The full error message has been printed in the console."));
                Debug.warn("There was a problem saving `" + mapsFile.getName() + "`. The error message has been printed below.");
                Debug.warn(e.getMessage());
                e.printStackTrace();
            }

            ReloadCommand.reloadConfig(game);
        } else {
            //sender.sendMessage(Main.colorize("&cYou did not specify the required number of arguments."));
            return false;
        }
        return false;
    }
}
