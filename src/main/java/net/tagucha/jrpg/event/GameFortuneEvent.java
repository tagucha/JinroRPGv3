package net.tagucha.jrpg.event;

import net.tagucha.jrpg.JinroGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameFortuneEvent extends GameEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Player forerunner;
    private final UUID target;

    public GameFortuneEvent(JinroGame game, Player forerunner, UUID target) {
        super(game);
        this.forerunner = forerunner;
        this.target = target;
    }

    public Player getForerunner() {
        return forerunner;
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
