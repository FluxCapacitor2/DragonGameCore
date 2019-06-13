package me.fluxcapacitor.dragongamecore;

import me.fluxcapacitor.dragongamecore.commands.*;
import me.fluxcapacitor.dragongamecore.inventories.GUI;
import me.fluxcapacitor.dragongamecore.inventories.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Main extends JavaPlugin {
    public static final ArrayList<DragonGame> games = new ArrayList<>();
    private static final String PREFIX = "&3Arcade &8>> &b";
    public static Main instance;
    static Scoreboard scoreboard;
    public GUIManager guiManager;

    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', PREFIX + string);
    }

    public static String colorizeWithoutPrefix(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    static boolean isBeta() {
        return instance.getDescription().getVersion().contains("ALPHA") | instance.getDescription().getVersion().contains("BETA");
    }

    @Override
    public void onEnable() {
        Main.instance = this;
        Main.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        this.guiManager = new GUIManager();
        Bukkit.getPluginManager().registerEvents(this.guiManager, this);

        if (isBeta()) Debug.subscribed.add(Bukkit.getConsoleSender());
        //Normal commands
        this.getCommand("joingui").setExecutor(new JoinGUICommand());
        this.getCommand("maps").setExecutor(new ListMapsCommand());
        this.getCommand("join").setExecutor(new JoinCommand());
        this.getCommand("leave").setExecutor(new LeaveCommand());
        this.getCommand("games").setExecutor(new ListGamesCommand());
        this.getCommand("spec").setExecutor(new SpectateCommand());
        //Admin commands
        this.getCommand("aaaddmap").setExecutor(new AddMapCommand());
        this.getCommand("aaremovemap").setExecutor(new RemoveMapCommand());
        this.getCommand("aaupdate").setExecutor(new UpdateCommand());
        this.getCommand("aastop").setExecutor(new StopCommand());
        this.getCommand("aareload").setExecutor(new ReloadCommand());
        this.getCommand("aadebug").setExecutor(new DebugCommand());
        this.getCommand("aaspawnpoint").setExecutor(new SpawnPointCommand());
        //Tab complete: Normal commands
        this.getCommand("joingui").setTabCompleter(this);
        this.getCommand("maps").setTabCompleter(this);
        this.getCommand("join").setTabCompleter(this);
        this.getCommand("leave").setTabCompleter(this);
        this.getCommand("games").setTabCompleter(this);
        this.getCommand("spec").setTabCompleter(this);
        //Tab complete: Admin commands
        this.getCommand("aaaddmap").setTabCompleter(this);
        this.getCommand("aaremovemap").setTabCompleter(this);
        this.getCommand("aaupdate").setTabCompleter(this);
        this.getCommand("aastop").setTabCompleter(this);
        this.getCommand("aareload").setTabCompleter(this);
        this.getCommand("aadebug").setTabCompleter(this);
        this.getCommand("aaspawnpoint").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = (Player) sender;
        List<String> complete = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("aaaddmap")) {
            if (args.length == 1) {
                // Command: /aaaddmap ***<game>*** <x1> <y1> <z1> <x2> <y2> <z2> <name>
                //Autocomplete game name
                for (DragonGame game : games) {
                    if (game.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        complete.add(game.getName());
                    }
                }
            } else if (args.length >= 2 && args.length <= 7) {
                // Command: /aaaddmap <game name>  ***<x1> <y1> <z1> <x2> <y2> <z2>*** <name>
                if (args.length == 2 || args.length == 5)
                    complete.add((String.valueOf(player.getTargetBlock((Set<Material>) null, 5).getX())));
                if (args.length == 3 || args.length == 6)
                    complete.add((String.valueOf(player.getTargetBlock((Set<Material>) null, 5).getY())));
                if (args.length == 4 || args.length == 7)
                    complete.add((String.valueOf(player.getTargetBlock((Set<Material>) null, 5).getZ())));
            }  //We can't autocomplete map ideas for you! Our work here is done.

        } else if (command.getName().equalsIgnoreCase("aaremovemap")) {
            if (args.length == 1) {
                //Autocomplete game name
                for (DragonGame game : games) {
                    if (game.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        complete.add(game.getName());
                    }
                }
            } else if (args.length == 2) {
                //Autocomplete map name
                DragonGame game = Wrapper.findGame(args[0]);
                if (game != null) {
                    for (GameMap map : game.getWrapper().maps) {
                        complete.add(map.name);
                    }
                }
            }
        } else if (
                command.getName().equalsIgnoreCase("aaupdate") |
                        command.getName().equalsIgnoreCase("aastop") |
                        command.getName().equalsIgnoreCase("aareload") |
                        command.getName().equalsIgnoreCase("join") |
                        command.getName().equalsIgnoreCase("joingui") |
                        command.getName().equalsIgnoreCase("maps") |
                        command.getName().equalsIgnoreCase("aaspawnpoint")) {
            //Autocomplete game name
            if (args.length == 1) {
                for (DragonGame game : games) {
                    if (game.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        complete.add(game.getName());
                    }
                }
            } else if (args.length == 2 && command.getName().equalsIgnoreCase("join") |
                    command.getName().equalsIgnoreCase("aaspawnpoint")) {
                //Autocomplete map name
                DragonGame game = Wrapper.findGame(args[0]);
                if (game != null) {
                    for (GameMap map : game.getWrapper().maps) {
                        complete.add(map.name);
                    }
                }
            } else if (args.length == 3 && command.getName().equalsIgnoreCase("aaspawnpoint")) {
                //Autocomplete team name
                DragonGame game = Wrapper.findGame(args[0]);
                if (game != null) {
                    GameMap map = game.getWrapper().findMap(args[1]);
                    if (map != null) {
                        for (Team t : map.getTeams()) {
                            complete.add(ChatColor.stripColor(t.getTeamName()));
                        }
                    }
                }
            }
        } else if (command.getName().equalsIgnoreCase("aadebug")) {
            complete.add("on");
            complete.add("off");
            complete.add("toggle");
        } else if (command.getName().equalsIgnoreCase("spec")) {
            //Autocomplete player name
            DragonGame game = Wrapper.findGame(player);
            if (game != null) {
                for (GameMap map : game.getWrapper().maps) {
                    for (Team t : map.getTeams()) {
                        for (Player p : t.getPlayers()) {
                            complete.add(p.getName());
                        }
                    }
                }
            }
        }
        Collections.sort(complete);
        return complete;
    }

    @Override
    public void onDisable() {
        ArrayList<Inventory> allInventories = new ArrayList<>();
        for (GUI gui : guiManager.guis) {
            allInventories.addAll(gui.inventories);
        }
        for (Inventory inventory : allInventories) {
            try {
                Player owner = (Player) inventory.getViewers().get(0);
                owner.closeInventory();
            } catch (Exception ignored) {

            }
        }
    }
}
