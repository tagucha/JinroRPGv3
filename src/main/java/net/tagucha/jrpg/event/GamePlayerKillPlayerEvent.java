package net.tagucha.jrpg.event;

import net.tagucha.jrpg.core.JinroGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GamePlayerKillPlayerEvent extends GameEvent {
    private static final HandlerList handlers = new HandlerList();
    private final UUID killer;
    private final UUID target;

    public GamePlayerKillPlayerEvent(JinroGame game, UUID killer, UUID target) {
        super(game);
        this.killer = killer;
        this.target = target;
    }

    public UUID getKiller() {
        return killer;
    }

    public UUID getTarget() {
        return target;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
