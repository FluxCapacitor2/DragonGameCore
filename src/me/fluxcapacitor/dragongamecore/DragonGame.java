package me.fluxcapacitor.dragongamecore;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main class for a minigame.
 * Each minigame instantiates this class to add basic features.
 */
public class DragonGame {
    private final Wrapper wrapper;

    private final String name;
    private final Main instance;

    private GameLifecycle gameLifecycle;
    private String startSubtitle;
    private String winnerSubtitle = "&eYou were the last player standing!";

    private boolean spectating = true;
    private boolean ignoreAir = false;
    private boolean disableDamage = false;
    private boolean fatalVoidDamage = true;
    private boolean FFA = true;

    private int countdownTime;
    private int startRequirement;
    private int maxPlayers;
    private ArrayList<Team> teams;

    @SuppressWarnings("unused")
    public DragonGame(String name) {
        this.name = name;
        this.wrapper = new Wrapper();
        this.instance = Main.instance;
        Main.games.add(this);
        Main.instance.getServer().getPluginManager().registerEvents(new EventListeners(this), Main.instance);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
        }
    }

    public Main getInstance() {
        return this.instance;
    }

    public String getName() {
        return this.name;
    }

    @SuppressWarnings("unused")
    public void loadConfig() {
        Wrapper wrapper = this.getWrapper();

        wrapper.maps = new ArrayList<>();
        File dataFolder = instance.getDataFolder();
        String mapsFilePath = getConfigFilePath();
        File mapsFile = new File(mapsFilePath);
        FileConfiguration maps = null;

        try {
            if (!dataFolder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dataFolder.mkdirs();
            }
            if (!mapsFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                mapsFile.createNewFile();
            }
            maps = YamlConfiguration.loadConfiguration(mapsFile);

        } catch (IOException e) {
            Debug.error("&cFailed to load maps file!");
            Debug.error("&cError message: " + e.getMessage());
        }
        if (maps != null) {
            ConfigurationSection mapsSection = maps.getConfigurationSection("maps");
            if (mapsSection != null) {
                Map<String, Object> allValues = mapsSection.getValues(false);
                Set<Map.Entry<String, Object>> values = allValues.entrySet();
                for (Map.Entry<String, Object> entry : values) {
                    String key = entry.getKey();
                    GameMap map = new GameMap(this, key, countdownTime, maxPlayers, startRequirement);
                    List<String> blocks = maps.getStringList("maps." + key);
                    Debug.info("&6Loaded " + blocks.size() + " blocks from the map '" + key + "'.");
                    if (!this.isFFA()) {
                        Debug.verbose("Adding all teams to " + key + ": " + teams.toString());
                        map.queue.teams.addAll(teams);
                    }
                    for (String blockString : blocks) {
                        map.addBlock(blockString);
                    }
                    Debug.verbose("Adding map to game: " + map.toString());
                    Debug.verbose(map.name + "'s FFA status: " + this.isFFA());
                    wrapper.maps.add(map);
                }
            }
        }
    }

    private String getConfigFilePath() {
        return this.getInstance().getDataFolder().getAbsolutePath() + "/game_" + this.getName() + ".yml";
    }

    @SuppressWarnings("unused")
    public ConfigurationSection getMapsSection() {
        return YamlConfiguration.loadConfiguration(getMapsFile()).getConfigurationSection("maps");
    }

    public File getMapsFile() {
        return new File(getConfigFilePath());
    }

    @SuppressWarnings("unused")
    public FileConfiguration getConfigObject() {
        return this.getInstance().getConfig();
    }

    public Wrapper getWrapper() {
        return this.wrapper;
    }

    public GameLifecycle getGameLifecycle() {
        return gameLifecycle;
    }

    @SuppressWarnings("unused")
    public void setGameLifecycle(GameLifecycle gameLifecycle) {
        this.gameLifecycle = gameLifecycle;
    }

    String getStartSubtitle() {
        return startSubtitle;
    }

    @SuppressWarnings("unused")
    public void setStartSubtitle(String startSubtitle) {
        this.startSubtitle = startSubtitle;
    }

    boolean isSpectatingEnabled() {
        return spectating;
    }

    @SuppressWarnings("unused")
    public void setSpectating(boolean spectating) {
        this.spectating = spectating;
    }

    boolean isIgnoreAir() {
        return ignoreAir;
    }

    @SuppressWarnings("unused")
    public void setIgnoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
    }

    @SuppressWarnings("WeakerAccess")
    public GameMap getMap(Player player) {
        for (GameMap map : this.getWrapper().maps) {
            for (Team t : map.queue.getTeams()) {
                if (t.getPlayers().contains(player)) return map;
            }
        }
        return null;
    }

    String getWinnerSubtitle() {
        return winnerSubtitle;
    }

    @SuppressWarnings("unused")
    public void setWinnerSubtitle(String winnerSubtitle) {
        this.winnerSubtitle = winnerSubtitle;
    }

    @SuppressWarnings("unused")
    public void setCountdownTime(int countdownTime) {
        this.countdownTime = countdownTime;
    }

    @SuppressWarnings("unused")
    public void setStartRequirement(int startRequirement) {
        this.startRequirement = startRequirement;
    }

    @SuppressWarnings("unused")
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    boolean isDisableDamage() {
        return disableDamage;
    }

    @SuppressWarnings("unused")
    public void setDisableDamage(boolean disableDamage) {
        this.disableDamage = disableDamage;
    }

    public boolean isFFA() {
        return FFA;
    }

    @SuppressWarnings("unused")
    public void setFFA(boolean FFA) {
        this.FFA = FFA;
    }

    @SuppressWarnings("unused")
    public void setTeams(ArrayList<Team> teams) {
        this.teams = teams;
    }

    public ArrayList<Team> getTeams() {
        return this.teams;
    }

    public boolean isFatalVoidDamage() {
        return fatalVoidDamage;
    }

    public void setFatalVoidDamage(boolean fatalVoidDamage) {
        this.fatalVoidDamage = fatalVoidDamage;
    }
}
