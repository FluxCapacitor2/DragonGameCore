package me.fluxcapacitor.dragongamecore.inventories;

import me.fluxcapacitor.dragongamecore.Debug;
import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import me.fluxcapacitor.dragongamecore.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.Objects;

public class GamesInventory extends GUI {
    @Override
    public void open(DragonGame game, Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Select game");
        for (int i = 0; i < Main.games.size(); i++) {
            DragonGame g = Main.games.get(i);
            inv.setItem(i, Items.createItem(g.getName(), Material.PAPER, new String[] {}));
        }
        player.openInventory(inv);
        this.inventories.add(inv);
    }

    @Override
    public void handleClick(DragonGame game, Wrapper wrapper, Player player, Inventory inventory, int slot, ClickType clickType) {
        String name = ChatColor.stripColor(inventory.getItem(slot).getItemMeta().getDisplayName());
        try {
            player.closeInventory();
            Main.instance.guiManager.joinInv.open(Objects.requireNonNull(Wrapper.findGame(name)), player);
            Debug.verbose("Opening join GUI from game select GUI: " + name);
        } catch (NullPointerException e) {
            Debug.verbose("Couldn't find game: " + name);
        }
    }
}
