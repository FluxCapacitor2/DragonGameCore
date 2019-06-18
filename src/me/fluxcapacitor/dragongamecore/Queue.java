package me.fluxcapacitor.dragongamecore;

import com.connorlinfoot.titleapi.TitleAPI;
import me.fluxcapacitor.dragongamecore.party.Party;
import me.fluxcapacitor.dragongamecore.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * A queue for a game, which is map-specific.
 *
 * @author FluxCapacitor
 * @see GameMap
 * @see DragonGame
 */
public class Queue {
    /**
     * The number of players required to start
     */
    final int START_REQUIREMENT;
    /**
     * The maximum players allowed in the game.
     */
    final int MAX_PLAYERS_PER_LOBBY;
    /**
     * The time it takes for the game to start after the timer starts.
     * Typically is 30.
     * Measured in seconds.
     *
     * @see CountdownTimer
     */
    private final int COUNTDOWN_TIME;
    /**
     * The map that this Queue is assigned.
     *
     * @see GameMap
     */
    private final GameMap map;
    /**
     * The game that this Queue is assigned.
     *
     * @see DragonGame
     */
    private final DragonGame game;
    /**
     * The "lifecycle" class with game-specific
     * methods that run at key points in the
     * game.
     *
     * @see GameLifecycle
     */
    private final GameLifecycle gameLifecycle;
    /**
     * The subtitle that all players see under the "GO" title.
     * This is shown when the game starts.
     */
    private final String gameStartSubtitle;
    /**
     * A list of all queued players.
     */
    public ArrayList<Player> queue;
    /**
     * A list of all teams ingame.
     */
    public ArrayList<Team> teams;
    /**
     * Shows if the timer has started.
     * If the game has started, this is still
     * <code>true</code>.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean timerStarted;
    /**
     * Reflects if the game has started.
     * Resets in <code>resetVariables()</code>.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean gameStarted;
    /**
     * A list of all spectators.
     */
    public ArrayList<Player> spectators;
    /**
     * The countdown timer used for starting the game.
     *
     * @see CountdownTimer
     */
    CountdownTimer timer;
    /**
     * Reflects if the game is allowed to declare a winner.
     * If this is <code>false</code>, there will never be
     * a winner until it is set to true (which happens when
     * the game starts).
     */
    boolean canDeclareWinner;
    /**
     * Shows if the game is currently being played by a party in
     * private mode.
     */
    boolean isInPrivate;
    /**
     * If <code>isInPrivate</code> is true, this will
     * be the party that is privately playing.
     *
     * @see Party
     */
    Party privateParty;

    /**
     * Create a new Queue
     *
     * @param game             The game that this queue is running
     * @param map              The map that the queue is being created for
     * @param countdownTime    The time that it takes to start the game. (Typically 30 seconds)
     * @param maxPlayers       The maximum players in the game
     * @param startRequirement The number of players required to start a game. Must be at least 2.
     * @see DragonGame
     * @see GameMap
     * @see Queue#COUNTDOWN_TIME
     * @see Queue#MAX_PLAYERS_PER_LOBBY
     * @see Queue#START_REQUIREMENT
     */
    Queue(DragonGame game, GameMap map, int countdownTime, int maxPlayers, int startRequirement) {
        this.queue = new ArrayList<>();
        //this.ingame = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.timerStarted = false;
        this.gameStarted = false;
        this.canDeclareWinner = false;
        this.map = map;
        this.game = game;
        this.gameLifecycle = game.getGameLifecycle();
        this.gameStartSubtitle = game.getStartSubtitle();
        if (Main.isBeta()) COUNTDOWN_TIME = 15;
        else COUNTDOWN_TIME = countdownTime;
        START_REQUIREMENT = startRequirement;
        MAX_PLAYERS_PER_LOBBY = maxPlayers;
        this.isInPrivate = false;
        this.privateParty = null;
    }

    /**
     * Add a player to the queue for a game.
     *
     * @param player The player to add to the queue
     */
    public void add(Player player) {

        if (PartyManager.isInParty(player)) {
            Debug.verbose(player.getName() + " IS in a party.");
            if (!PartyManager.isLeader(player)) {
                Debug.verbose("...They are not the leader.");
                player.sendMessage(Main.colorize("&cYou must be the party leader to queue for a game!"));
            } else {
                Debug.verbose("Player that tried to join IS the leader. Joining...");
                //They are the leader
                //Add them and their whole team to the game (on the same team if it's a team game)
                Party party = PartyManager.findParty(player);
                assert party != null;
                boolean isPriority = party.leader.hasPermission("arcade.priorityqueue");
                boolean isPrivate = party.isPrivate;
                if (isPrivate) {
                    this.isInPrivate = true;
                    this.privateParty = party;
                }
                for (Player p : party.players) {
                    p.sendMessage(PartyManager.colorize("&aThe party leader has queued for &f" + game.getName() + "&a."));
                    addInternal(p, isPriority, isPrivate);
                    Debug.verbose("Adding " + p.getName() + " to the game of " + game.getName() + ".");
                }
                Debug.verbose("All players should have joined. Private: " + isPrivate + ", priority: " + isPriority + ".");
                this.updateQueue();
            }
        } else {
            Debug.verbose("Just adding " + player.getName() + " normally because they are not in a party.");
            if (this.isInPrivate) {
                player.sendMessage(Main.colorize("&cThis map is currently hosting a private game so you were added to the queue."));
            }
            addInternal(player);
        }
    }

    /**
     * Add a player to the Queue.
     *
     * @param player The player to add to the queue.
     */
    private void addInternal(Player player) {
        this.addInternal(player, player.hasPermission("arcade.priorityqueue"), false);
    }

    /**
     * This is the main method for adding a player to a game.
     * All overloaded methods called <code>addInternal</code> or <code>add</code>
     * eventually call this method after handling parties and other complications.
     *
     * @param player     The player to add to the game
     * @param isPriority If the player has permission for priority queue
     */
    private void addInternal(Player player, boolean isPriority) {
        //Make sure they aren't already queueing for something else
        for (DragonGame game1 : Main.games) {
            for (GameMap map : game1.getWrapper().maps) {
                if (map.queue.queue.contains(player)) {
                    player.sendMessage(Main.colorize("&cYou are already queued for &f" + game1.getName() + "&c on &f" + map.name + "&c! Type &f/leave&c to leave that queue before joining another one."));
                    return;
                }
            }
        }
        if (!this.gameStarted && this.timerStarted && this.teams.size() + 1 <= MAX_PLAYERS_PER_LOBBY) {
            //They can join a game that hasn't started yet!
            //Do all of the stuff we need to do for each player (like sending titles & putting them into adventure mode)
            pregamePerPlayer(game, map, player);
            //Take 2 seconds off the timer by calling run() twice (which takes off 1 second each)
            this.timer.run();
            this.timer.run();
        } else {
            //There's already a game being played. They will just chill in the queue for now.
            if (isPriority) {
                //Put them in front of people without priority queue
                boolean added = false;
                for (int i = 0; i < this.queue.size(); i++) {
                    Player p = this.queue.get(i);
                    if (!p.hasPermission("arcade.priorityqueue")) {
                        this.queue.add(i, player);
                        added = true;
                        break;
                    }
                }
                if (!added) this.queue.add(player);
            } else {
                //They don't have priority queue. Just put them at the back of the queue.
                this.queue.add(player);
            }
            if (!PartyManager.isInParty(player))
                player.sendMessage(Main.colorize("&aYou have queued for &f" + game.getName() + "&a on &f" + map.name + "&a. You are at position " +
                        "&f" + (this.queue.indexOf(player) + 1) + "&a/&f" + this.queue.size() + "&a in the queue."));
        }
    }

    /**
     * Update the queue and start the timer if necessary.
     */
    private void updateQueue() {
        Debug.verbose("Updating queue...");
        int privatePartyIndex = getPrivatePartyIndex();
        Debug.verbose("Private party index: " + privatePartyIndex);
        if (privatePartyIndex == 0) {
            //The private party is at the beginning of the line, let them in!
            if (!this.gameStarted && !this.timerStarted) {
                Debug.verbose("Starting timer because private party was first in queue...");
                this.startTimer();
            }
        } else {
            Debug.verbose("Non-private player count: " + getNonPrivatePlayerCount());
            Debug.verbose("Game started: " + this.gameStarted + ", Timer started: " + this.timerStarted + ".");
            if (getNonPrivatePlayerCount() >= START_REQUIREMENT && !this.gameStarted && !this.timerStarted) {
                this.startTimer();
            }
        }
    }

    /**
     * Get the count of players that are not in private parties.
     *
     * @return The count of players that are not in a private party.
     */
    private int getNonPrivatePlayerCount() {
        int count = 0;
        for (Player p : this.queue) {
            Party party = PartyManager.findParty(p);
            if ((party != null && !party.isPrivate) | (party == null)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Add a player to the queue
     *
     * @param player     The player to add to the queue
     * @param isPriority If the player has priority queue
     * @param isPrivate  If the player is joining in private
     */
    private void addInternal(Player player, boolean isPriority, boolean isPrivate) {
        if (this.isInPrivate) {
            if (isPrivate) {
                if (this.privateParty.players.contains(player)) {
                    //Continue into the game because they are in the party that "owns" this game.
                    Party party = PartyManager.findParty(player);
                    assert party != null;

                    boolean queued = false;
                    for (Team t : this.teams) {
                        for (Player p : t.getPlayers()) {
                            if (!party.players.contains(p)) {
                                //Wait in queue for the next game on this map to open up
                                this.queue.add(player);
                                this.updateQueue();
                                queued = true;
                                break;
                            }
                        }
                    }
                    if (!queued) this.addInternal(player, isPriority);
                }
            } else {
                //Deny access & put them in queue
                this.queue.add(player);
                this.updateQueue();
            }
        } else {
            this.addInternal(player, isPriority);
        }
    }

    /**
     * Get the name of the world that the map is in.
     *
     * @return The world name
     */
    String getWorldName() {
        return this.map.getBlocks().get(0).getWorldName();
    }

    /**
     * When the timer starts, this method is triggered. It resets the map and other related things.
     */
    private void preGame() {
        //Reset the map
        this.map.reset();
        //Put the top 10 players in the queue into the game
        int privatePartyIndex = getPrivatePartyIndex();
        Party privateParty = null;
        if (privatePartyIndex != -1) privateParty = PartyManager.findParty(this.queue.get(privatePartyIndex));

        Debug.verbose("Private party index: " + privatePartyIndex);
        Debug.verbose("Private party: " + privateParty);
        if (privatePartyIndex == 0) {
            Debug.verbose("Only allowing private party to play because they are first in queue");
            //The private party is first in line, allow them into the game! (but not anyone else that isn't in the party)
            for (Iterator<Player> it = this.queue.iterator(); it.hasNext(); ) {
                Player p = it.next();
                //Check if they are in the party
                if (Objects.equals(PartyManager.findParty(p), privateParty)) {
                    Debug.verbose("Allowing " + p.getName() + " into the game because they were in a private party");
                    //Let them in!
                    if (game.isFFA()) {
                        this.addTeam(new Team(p));
                    }
                    pregamePerPlayer(game, map, p);
                    it.remove();
                }
            }
        } else {
            Debug.verbose("Just running normally because a private party is not first in the queue");
            for (Iterator<Player> it = this.queue.iterator(); it.hasNext(); ) {
                Player p = it.next();
                Party party = PartyManager.findParty(p);
                if (!Objects.equals(party, privateParty)) {
                    if (game.isFFA()) this.addTeam(new Team(p));
                    pregamePerPlayer(game, map, p);
                    it.remove();
                    Debug.verbose("Added " + p.getName() + " to the game.");
                } else {
                    Debug.verbose("Not adding " + p.getName() + " because they are part of a private party that is not first.");
                }
            }
        }
        //Make everyone invincible
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag __global__ invincible -w " + getWorldName() + " allow");
        //Run game-specific pregame method.
        gameLifecycle.pregame(game, map);
    }

    /**
     * Get the index of a private party in the queue.
     * If the returned value is 0, then a private party is first in the queue.
     *
     * @return The index of the first private party in the queue
     */
    private int getPrivatePartyIndex() {
        int privatePartyIndex = -1;
        for (int i = 0; i < this.queue.size(); i++) {
            Player player = this.queue.get(i);
            Party party = PartyManager.findParty(player);

            if (party != null) {
                if (party.isPrivate) {
                    //They're in a private party
                    //noinspection ConstantConditions
                    if (privatePartyIndex == -1) {
                        privatePartyIndex = i;
                        privateParty = party;
                        break;
                    }
                }
            }
        }
        return privatePartyIndex;
    }

    /**
     * This is like the pregame method, but it is called per-player.
     *
     * @param game   The game that the player is playing
     * @param map    The map that the player is playing on
     * @param player The player to use within the method
     */
    private void pregamePerPlayer(DragonGame game, GameMap map, Player player) {
        Debug.verbose("Running pregame per-player for " + player.getName());
        //Is this game free-for-all or are there already-defined teams?
        if (game.isFFA()) {
            //If it's FFA, make a team under the player's name
            Team newTeam = new Team(player);
            //Make sure the "new" team is not a duplicate
            if (!newTeam.matches(this.teams)) {
                //It's not a duplicate. Go ahead & add it!
                Debug.info("Creating new team for FFA player: " + newTeam.getTeamName());
                this.teams.add(newTeam);
            }
        } else {
            Team preferredTeam = null;
            try {
                FileConfiguration config = Main.instance.getConfig();
                File file = game.getMapsFile();
                config.load(file);
                String query = (String) config.get("playerdata.preferredTeams." + player.getUniqueId());
                for (Team t : teams) {
                    if (ChatColor.stripColor(t.getTeamName()).equals(ChatColor.stripColor(query))) {
                        //This is their preferred team!
                        preferredTeam = t;
                    }
                }
            } catch (IOException | InvalidConfigurationException e) {
                Debug.warn("There was an error saving / loading the config file for " + game.getName() + ".");
                Debug.warn("The error is printed below.");
                Debug.warn(e.getMessage());
                e.printStackTrace();
            }

            if (preferredTeam != null) {
                preferredTeam.addPlayer(player);
            } else {
                //They didn't select a preferred team: assign them to the team with the least players on it.
                int min = this.teams.get(0).getPlayers().size();
                Team best = this.teams.get(0);
                for (Team t : this.teams) {
                    if (t.getPlayers().size() < min) {
                        min = t.getPlayers().size();
                        best = t;
                    }
                }
                best.addPlayer(player);
                Debug.verbose("Added " + player.getName() + " to the team " + best.getTeamName());
            }
        }
        //Teleport them to the map's spawn point
        try {
            Location spawnPoint = this.getSpawnPoint(player);
            player.teleport(spawnPoint);
        } catch (NullPointerException e) {
            for (Team t : this.teams) {
                for (Player pl : t.getPlayers()) {
                    pl.sendMessage(Main.colorize("&aThe creator of this map did not set a spawn point! Tell a server administrator to fix this."));
                    Debug.error("There is no spawn point set for the map " + map.name + " in the game " + game.getName() + "!");
                }
            }
        }
        //Heal everyone & set their max health to normal
        player.setMaxHealth(20.0D);
        player.setHealth(20.0D);
        //Run the game-specific pregame per-player method
        gameLifecycle.pregamePerPlayer(game, map, player);
    }

    /**
     * Get the team-specific spawn point of the map
     *
     * @param player The player to use to find a spawn point
     * @return A <code>Location</code> that is a player's spawn point (which is team specific)
     */
    private Location getSpawnPoint(Player player) {
        return this.map.getSpawnPoint(player);
    }

    /**
     * Convert this class to a String
     *
     * @return A human-readable String that represents this class
     */
    @Override
    public String toString() {
        return "\n       Ingame: " + this.teams.toString() +
                "\n       Spectating: " + this.spectators.toString() +
                "\n       Queued: " + this.queue.toString();
    }

    /**
     * Start the game.
     * This method balances teams, removes friendly fire, turns off
     * invincibility, etc.
     */
    private void startGame() {
        //Balance teams
        this.balanceTeams();
        //Turn off friendly fire
        for (Team t : teams) {
            t.removeFriendlyFire(game);
        }
        //Reset the players' invincibility
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "rg flag __global__ invincible -w " + map.queue.getWorldName());
        for (Team t : this.teams) {
            for (Player p : t.getPlayers()) {
                TitleAPI.sendTitle(p, 20, 60, 20, Main.colorizeWithoutPrefix("&a&lGO"), Main.colorizeWithoutPrefix(this.gameStartSubtitle));
            }
        }
        //Run game-specific on game start method
        gameLifecycle.onGameStart(game, map);
        for (Team t : this.teams) {
            for (Player p : t.getPlayers()) {
                gameLifecycle.onGameStartPerPlayer(game, map, p);
            }
        }
        //To start the code that declares a winner on death and other things, set the game started to true
        this.gameStarted = true;
        this.canDeclareWinner = true;
    }

    /**
     * This method balances teams.
     * If one team (x) has 2 more players
     * than another (y), then <code>y</code> will take one
     * player from <code>x</code>.
     */
    private void balanceTeams() {
        for (Team x : this.teams) {
            for (Team y : this.teams) {
                if (x.getPlayers().size() >= y.getPlayers().size() + 2) {
                    //The team is unbalanced. Move a player from x to y
                    Player last = x.getPlayers().get(x.getPlayers().size() - 1);
                    x.removePlayer(last);
                    y.addPlayer(last);
                    last.teleport(map.getSpawnPoint(y));
                    last.sendMessage(Main.colorize("&aDue to team balancing, you were moved to the " + y.getTeamName() + "&r&a team."));
                    //If they're still unbalanced, call the method again because we won't get to come back to this.
                    if (x.getPlayers().size() >= y.getPlayers().size() + 2) balanceTeams();
                }
            }
        }
    }

    /**
     * This method is called every second (or every time the timer <code>run</code>s)
     *
     * @param timer The timer used for determining how many seconds are left.
     */
    private void countDown(CountdownTimer timer) {
        int secondsLeft = timer.getSecondsLeft();
        gameLifecycle.everySecond(game, map, timer);
        if (secondsLeft % 10 == 0 || secondsLeft <= 5) {
            for (Team team : this.teams) {
                for (Player p : team.getPlayers()) {
                    TitleAPI.sendTitle(p, 20, 20, 20, Main.colorizeWithoutPrefix("&f" + secondsLeft + "&a seconds!"), Main.colorizeWithoutPrefix("&aType &f/leave&a to leave the game."));
                    gameLifecycle.everySecondPerPlayer(game, map, timer, p);
                }
            }
        }
    }

    /**
     * Setup the next game.
     * If there are enough people in queue, then start the next game!
     */
    void setupNextGame() {
        if (this.teams.size() == 0) {
            //The game is over & the winner was removed from the game as well.
            //Move the next 10 people from the queue into a game (IF there are enough people in the queue)
            if (this.queue.size() >= START_REQUIREMENT) {
                //There is enough people in queue for this map. It is okay to start the pregame again.
                // Queue#startTimer() will run Queue#preGame() as soon as it is triggered, so we don't need to put that here.
                this.startTimer();
                this.canDeclareWinner = false;
            }
            //If this did not trigger, then there aren't enough people to start. When someone joins, it will be handled by `Queue#add(Player)`
        }
    }

    /**
     * Start the timer.
     * This also sets <code>timerStarted</code> to <code>true</code>
     * and calls the game-specific <code>onTimerStart</code> and <code>onTimerStartPerPlayer</code>
     * methods.
     */
    private void startTimer() {
        //Code adapted from a forum post by @ExpDev on SpigotMC.org
        this.timer = this.newTimer();
        this.timerStarted = true;
        timer.scheduleTimer();
        gameLifecycle.onTimerStart(game, map);
        for (Team t : this.teams) {
            for (Player p : t.getPlayers()) {
                gameLifecycle.onTimerStartPerPlayer(game, map, p);
            }
        }
    }

    /**
     * Create a new timer
     *
     * @return A new timer object to use in starting the game
     */
    private CountdownTimer newTimer() {
        return new CountdownTimer(Main.instance, COUNTDOWN_TIME, this::preGame, this::startGame, this::countDown);
    }

    /**
     * Reset most variables associated with this Queue.
     * This includes <code>gameStarted</code>,
     * <code>timerStarted</code>, and <code>timer</code>.
     */
    public void resetVariables() {
        for (Team t : teams) {
            if (t.team != null) t.team.unregister();
        }
        this.canDeclareWinner = false;
        this.timerStarted = false;
        this.gameStarted = false;
        if (this.timer != null) this.timer.cancelTimer();
        this.timer = this.newTimer();
        this.map.reset();
        this.removeAllTeams();
        this.isInPrivate = false;
        this.privateParty = null;
    }

    /**
     * Reset all ingame players and spectators.
     * Mostly used in conjunction with <code>resetVariables()</code>
     *
     * @see Queue#resetVariables()
     */
    public void resetIngame() {
        this.teams = new ArrayList<>();
        this.spectators = new ArrayList<>();
    }

    /**
     * Reset all queued players.
     * Mostly used in conjunction with <code>resetVariables()</code>
     *
     * @see Queue#resetVariables()
     */
    public void resetQueue() {
        this.queue = new ArrayList<>();
    }

    /**
     * Get all teams in the game
     *
     * @return All teams in the game
     */
    public ArrayList<Team> getTeams() {
        return teams;
    }

    /**
     * Add a team to the game
     *
     * @param team The team to add
     */
    private void addTeam(Team team) {
        this.teams.add(team);
    }

    /**
     * Reset all teams
     *
     * @see Queue#resetVariables()
     */
    private void removeAllTeams() {
        this.teams = new ArrayList<>();
    }

    /**
     * Get the number of teams with players on them
     *
     * @return The count of all teams with at least 1 player on them
     */
    public int getTeamCountWithPlayers() {
        int count = 0;
        for (Team t : this.teams) {
            if (t.getPlayers().size() > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the number of players in the game.
     *
     * @return Total ingame player count.
     */
    public int getPlayerCount() {
        int count = 0;
        for (Team t : this.teams) {
            count += t.getPlayers().size();
        }
        return count;
    }

    /**
     * Declare a winner
     *
     * @param player The winner
     * @deprecated
     */
    @Deprecated
    public void declareWinner(Player player) {
        if (canDeclareWinner) {
            teams.forEach(o -> map.queue.spectators.addAll(o.getPlayers()));
            spectators.remove(player);
            if (game.isFFA()) {
                teams.removeIf(t -> !t.players.get(0).getUniqueId().equals(player.getUniqueId()));
            } else {
                for (Team t : teams) {
                    if (!t.players.contains(player)) {
                        teams.remove(t);
                    }
                }
            }
            Debug.verbose("A winner was declared for " + game.getName() + " on " + map.name + ".");
        } else {
            Debug.verbose(game.getName() + " on " + map.name + " tried to declare a winner, but was not allowed to.");
        }
        game.getWrapper().updatePlayerCounts();
        removeAllTeams();
        setupNextGame();
    }

    /**
     * Remove a player from ALL of their team(s).
     *
     * @param player The player to remove from their team(s).
     */
    public void removePlayerFromTeams(Player player) {
        for (Iterator it = teams.iterator(); it.hasNext(); ) {
            Team t = (Team) it.next();
            t.removePlayer(player);
            Debug.verbose(player.getName() + " was removed from their teams.");
            if (game.isFFA() && t.getPlayers().size() == 0) {
                it.remove();
                Debug.verbose("The team " + t.getTeamName() + " was removed because there were no players after the removal of " + player.getName() + ".");
            }
        }
    }
}
