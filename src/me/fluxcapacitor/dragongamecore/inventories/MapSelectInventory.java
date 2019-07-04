package me.fluxcapacitor.dragongamecore.inventories;

import me.clip.placeholderapi.PlaceholderAPI;
import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.GameMap;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import me.fluxcapacitor.dragongamecore.util.Items;
import me.fluxcapacitor.dragongamecore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public class MapSelectInventory extends GUI {
    @Override
    public void open(DragonGame game, Player player) {
        if (player.hasPermission("arcade.selectmap")) {
            if (game != null) {
                Inventory inv = Bukkit.createInventory(null, 54, game.getName());
                for (GameMap map : game.getWrapper().maps) {
                    inv.addItem(Items.createItem("&a" + Text.toTitleCase(map.name), Material.PAPER, new String[] {
                            "&7" + PlaceholderAPI.setPlaceholders(player, "%arcade." + game.getName() + "_status_" + map.name + "%"),
                            "",
                            "&aLeft click to join",
                            "&7Right click to spectate"
                    }));
                }
                inv.setItem(45, Items.createItem("&cBack", Material.ARROW, new String[] {}));
                inv.setItem(49, Items.createItem("&eRefresh", Material.DOUBLE_PLANT, new String[] {}));
                player.openInventory(inv);
                this.inventories.add(inv);
            }
        } else {
            player.sendMessage(Main.colorizeWithoutPrefix("&cYou must be at least &aPremium&c to execute this command."));
        }
    }

    @Override
    public void handleClick(DragonGame game, Wrapper wrapper, Player player, Inventory inventory, int slot, ClickType clickType) {
        if (slot == 49) {
            //They presssed the refresh button. Just close the GUI and open it again.
            player.closeInventory();
            Main.instance.guiManager.mapInv.open(game, player);
        } else if (slot == 45) {
            //They pressed the back button. Route them back to the main GUI.
            player.closeInventory();
            Main.instance.guiManager.joinInv.open(game, player);
        } else {
            //They must've clicked on a map! Have them join that map.
            try {
                GameMap map = wrapper.maps.get(slot);
                if (clickType.equals(ClickType.RIGHT) | clickType.equals(ClickType.SHIFT_RIGHT)) {
                    Wrapper.spectate(map, player);
                } else {
                    map.queue.add(player);
                }
                player.closeInventory();
            } catch (Exception ignored) {
                //They might have clicked on air...
                //We couldn't find the map, so we can't really do anything about this.
            }
        }
    }
}
