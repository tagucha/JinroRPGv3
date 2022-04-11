package net.tagucha.jrpg.event;

import net.tagucha.jrpg.core.JinroGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameUseEyeOfMadEvent extends GameEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Player mad;
    private final UUID target;

    public GameUseEyeOfMadEvent(JinroGame game, Player mad, UUID target) {
        super(game);
        this.mad = mad;
        this.target = target;
    }

    public Player getMad() {
        return mad;
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
