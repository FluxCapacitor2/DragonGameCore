package me.fluxcapacitor.dragongamecore.inventories;

import me.fluxcapacitor.dragongamecore.DragonGame;
import me.fluxcapacitor.dragongamecore.Main;
import me.fluxcapacitor.dragongamecore.Wrapper;
import me.fluxcapacitor.dragongamecore.commands.LeaveCommand;
import me.fluxcapacitor.dragongamecore.util.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public class JoinInventory extends GUI {
    @Override
    public void open(DragonGame game, Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, game.getName());
        //Quick join
        inv.setItem(12, Items.createItem("&aQuick Join", Material.PAPER, new String[]
                {"&7BlueDragon will decide what game", "&7will start fastest, and put you", "&7right in."}));
        //Choose team
        inv.setItem(5, Items.createItem("&eSelect Preferred Team", Material.GOLD_SWORD, new String[]
                {"&7Pick a team that you prefer to", "&7join before you join a game."}));
        //Game info
        inv.setItem(13, Items.createItem("&e&l" + game.getName(), Material.EMERALD, new String[]
                {"&fClick for game information"}));
        //Map selector
        inv.setItem(14, Items.createItem("&dMap Selector", Material.BOOK_AND_QUILL, new String[]
                {"&cRequires &aPremium", "&7Like a certain map? Pick any", "&7one you want. The game may take",
                        "&7a little longer to start, but it'll be", "&7worth the wait."}));
        //Leave game
        if (game.getWrapper().isQueuedOrIngameOrSpectating(player)) {
            inv.setItem(22, Items.createItem("&cLeave Queue/Game", Material.BED, new String[]
                    {"&7Leave the current game or ", "&7queue that you are in."}));
        }
        player.openInventory(inv);
        this.inventories.add(inv);
    }

    @Override
    public void handleClick(DragonGame game, Wrapper wrapper, Player player, int slot, ClickType clickType) {
        switch (slot) {
            case 5:
                //Select preferred team
                player.closeInventory();
                Main.instance.guiManager.teamInv.open(game, player);
                break;
            case 12:
                //Random map.
                wrapper.findMostPopulatedMap().queue.add(player);
                player.closeInventory();
                break;
            case 13:
                //Give more information about the game.
                Bukkit.dispatchCommand(player, "info " + game.getName());
                player.closeInventory();
                break;
            case 14:
                //Open the map selector.
                player.closeInventory();
                Main.instance.guiManager.mapInv.open(game, player);
                break;
            case 22:
                //Unqueue from the game.
                LeaveCommand.removePlayerFromGame(player, game);
                player.closeInventory();
                break;
        }
    }
}
