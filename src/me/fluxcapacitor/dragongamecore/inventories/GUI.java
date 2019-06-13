package me.fluxcapacitor.dragongamecore.inventories;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Wrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public abstract class GUI {

    public ArrayList<Inventory> inventories = new ArrayList<>();

    public abstract void open(DragonGame game, Player player);

    public abstract void handleClick(DragonGame game, Wrapper wrapper, Player player, Inventory inventory, int slot, ClickType clickType);
}
