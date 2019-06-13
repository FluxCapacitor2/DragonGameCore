package me.fluxcapacitor.dragongamecore.inventories;

import me.fluxcapacitor.dragongamecore.*;
import me.fluxcapacitor.dragongamecore.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TeamSelectInventory extends GUI {
    @Override
    public void open(DragonGame game, Player player) {
        if (!game.isFFA()) {
            Inventory inv = Bukkit.createInventory(null, 27, game.getName());
            ArrayList<Team> teams = game.getTeams();
            String[] lore = new String[] {"&7Click to select preferred team.", "&7You will be put on this team every game", "&7before team balancing."};
            if (teams.size() == 2) {
                inv.setItem(11, Items.createItem(teams.get(0).getTeamName(), teams.get(0).getMaterial(), lore));
                inv.setItem(15, Items.createItem(teams.get(1).getTeamName(), teams.get(1).getMaterial(), lore));
            }
            inv.setItem(18, Items.createItem("&cBack", Material.ARROW, new String[] {}));
            player.openInventory(inv);
            this.inventories.add(inv);
        } else {
            player.sendMessage(Main.colorize("&cYou can not select a preferred team for a FFA game!"));
        }
    }

    @Override
    public void handleClick(DragonGame game, Wrapper wrapper, Player player, Inventory inventory, int slot, ClickType clickType) {
        ItemStack item = inventory.getItem(slot);
        String name = item.getItemMeta().getDisplayName();
        Debug.verbose("User clicked on item: " + name);
        for (Team t : game.getTeams()) {
            if (ChatColor.stripColor(t.getTeamName()).equals(ChatColor.stripColor(name))) {
                Debug.verbose("Trying to set preferred team...");
                //They clicked on this team as their preferred team
                FileConfiguration config = Main.instance.getConfig();
                File mapsFile = game.getMapsFile();
                Debug.verbose("Loading config file: " + mapsFile.getName());
                try {
                    config.load(mapsFile);
                    config.set("playerdata.preferredTeams." + player.getUniqueId(), t.getTeamName());
                    config.save(mapsFile);
                    player.closeInventory();
                    Debug.info("Set preferred team to " + t.getTeamName() + "&r for " + player.getName());
                    player.sendMessage(Main.colorize("&bYour preferred team was set to &f" + t.getTeamName() + "&r&b."));
                } catch (IOException | InvalidConfigurationException exception) {
                    Debug.warn("There was an error saving / loading the config file for " + game.getName() + ".");
                    Debug.warn("The error is printed below.");
                    Debug.warn(exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
        if (slot == 18) {
            //Back button: send them back to the join GUI
            player.closeInventory();
            Main.instance.guiManager.joinInv.open(game, player);
        }
    }
}
