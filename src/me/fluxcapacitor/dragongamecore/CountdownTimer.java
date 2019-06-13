package me.fluxcapacitor.dragongamecore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

/**
 * Class created by
 *
 * @author ExpDev
 * and modified by
 * @author FluxCapacitor
 * to suit the needs of BlueDragon.
 */
@SuppressWarnings("WeakerAccess")
public class CountdownTimer implements Runnable {

    // Main class for bukkit scheduling
    private final JavaPlugin plugin;
    // Seconds and seconds left
    private final int seconds;
    // Actions to perform while counting down, before and after
    private final Consumer<CountdownTimer> everySecond;
    private final Runnable beforeTimer;
    private final Runnable afterTimer;
    // Our scheduled task's assigned id, needed for canceling
    private Integer assignedTaskId;
    private int secondsLeft;

    // Construct a timer, you could create multiple so for example if
    // you do not want these "actions"
    public CountdownTimer(JavaPlugin plugin, int seconds,
                          Runnable beforeTimer, Runnable afterTimer,
                          Consumer<CountdownTimer> everySecond) {
        // Initializing fields
        this.plugin = plugin;

        this.seconds = seconds;
        this.secondsLeft = seconds;

        this.beforeTimer = beforeTimer;
        this.afterTimer = afterTimer;
        this.everySecond = everySecond;
    }

    /**
     * Runs the timer once, decrements seconds etc...
     * Really wish we could make it protected/private so you couldn't access it
     */
    @Override
    public void run() {
        // Is the timer up?
        if (secondsLeft < 1) {
            // Do what was supposed to happen after the timer
            afterTimer.run();

            // Cancel timer
            if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
            return;
        }

        // Are we just starting?
        if (secondsLeft == seconds) beforeTimer.run();

        // Do what's supposed to happen every second
        everySecond.accept(this);

        // Decrement the seconds left
        secondsLeft--;
        //Bukkit.getConsoleSender().sendMessage("[TIMER #" + this.assignedTaskId + "] Decremented 1 second. Seconds left after this: " + secondsLeft);
    }

    /*
      Gets the total seconds this timer was set to run for

      @return Total seconds timer should run
     */
    /*
    public int getTotalSeconds() {
        return seconds;
    }
     */

    /**
     * Gets the seconds left this timer should run
     *
     * @return Seconds left timer should run
     */
    public int getSecondsLeft() {
        return secondsLeft;
    }

    /**
     * Schedules this instance to "run" every second
     */
    public void scheduleTimer() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
        //Bukkit.getConsoleSender().sendMessage("Started timer with task id '" + this.assignedTaskId + "'.");
    }

    /**
     * Cancels the timer
     */
    public void cancelTimer() {
        try {
            if (this.assignedTaskId != null) {
                //Bukkit.getConsoleSender().sendMessage("The timer with task id '" + this.assignedTaskId + "' was cancelled.");
                Bukkit.getScheduler().cancelTask(this.assignedTaskId);
            }
        } catch (NullPointerException e) {
            //There was no timer at this.assignedTaskId's index so it couldn't be cancelled. There isn't much we can do about this, but it doesn't really matter anyway.
        }
    }

}