package me.fluxcapacitor.dragongamecore.inventories;

import me.fluxcapacitor.dragongamecore.Debug;
import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class GUIManager implements Listener {

    public JoinInventory joinInv;
    public MapSelectInventory mapInv;
    public TeamSelectInventory teamInv;
    public GamesInventory gamesInv;
    public ArrayList<GUI> guis;

    public GUIManager() {
        this.joinInv = new JoinInventory();
        this.mapInv = new MapSelectInventory();
        this.teamInv = new TeamSelectInventory();
        this.gamesInv = new GamesInventory();
        this.guis = new ArrayList<>();

        this.guis.add(joinInv);
        this.guis.add(mapInv);
        this.guis.add(teamInv);
        this.guis.add(gamesInv);
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        for (GUI gui : guis) {
            if (gui.inventories.contains(inventory)) {
                event.setCancelled(true);
                DragonGame game = Wrapper.findGame(inventory.getTitle());
                Player player = (Player) event.getWhoClicked();
                Wrapper wrapper = null;
                if (game != null) wrapper = game.getWrapper();
                int slot = event.getSlot();
                if (inventory.getItem(slot) != null && !inventory.getItem(slot).getType().equals(Material.AIR)) {
                    Debug.verbose(String.format("Handling GUI click: slot = %s, player = %s, inventory = %s, item = %s", slot, player, inventory, inventory.getItem(slot)));
                    gui.handleClick(game, wrapper, player, inventory, slot, event.getClick());
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onInventoryClose(InventoryCloseEvent event) {
        for (GUI gui : guis) {
            gui.inventories.remove(event.getInventory());
        }
    }
}
