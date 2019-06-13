package me.fluxcapacitor.dragongamecore.util;

import me.fluxcapacitor.dragongamecore.Main;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Items {
    public static ItemStack createItem(String name, Material material, String[] lore) {
        return createItem(name, new ItemStack(material, 1), lore);
    }

    public static ItemStack createItem(String name, ItemStack item, String[] lore) {
        for (int i = 0; i < lore.length; i++) {
            lore[i] = Main.colorizeWithoutPrefix(lore[i]);
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Main.colorizeWithoutPrefix("&r" + name));
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack setBannerColor(ItemStack banner, DyeColor color) {
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        meta.setBaseColor(color);
        banner.setItemMeta(meta);
        return banner;
    }
}
