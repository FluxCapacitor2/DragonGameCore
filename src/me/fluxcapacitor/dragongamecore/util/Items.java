package me.fluxcapacitor.dragongamecore.util;

import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Items {
    public static ItemStack createItem(String name, Material material, String[] lore) {
        for (int i = 0; i < lore.length; i++) {
            lore[i] = Main.colorizeWithoutPrefix(lore[i]);
        }
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Main.colorizeWithoutPrefix("&r" + name));
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
