package me.fluxcapacitor.dragongamecore;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * The "lifecycle" for the game. Contains many methods that are called in their corresponding situations.
 *
 * @author FluxCapacitor
 */
@SuppressWarnings("WeakerAccess")
public abstract class GameLifecycle {

    public abstract void pregame(DragonGame game, GameMap map);

    public abstract void pregamePerPlayer(DragonGame game, GameMap map, Player player);

    public abstract void everySecond(DragonGame game, GameMap map, CountdownTimer timer);

    public abstract void everySecondPerPlayer(DragonGame game, GameMap map, CountdownTimer timer, Player player);

    public abstract void onGameStart(DragonGame game, GameMap map);

    public abstract void onGameStartPerPlayer(DragonGame game, GameMap map, Player player);

    public abstract void onTimerStart(DragonGame game, GameMap map);

    public abstract void onTimerStartPerPlayer(DragonGame game, GameMap map, Player player);

    public abstract void onPlayerKill(DragonGame game, GameMap map, Player killer, Player killed, EntityDeathEvent event);

    public abstract void onPlayerRespawn(DragonGame game, GameMap map, Player respawned, PlayerRespawnEvent respawnEvent);

    public abstract void onPlayerWin(DragonGame game, GameMap map, Player winner);
}
