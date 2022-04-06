package net.tagucha.jrpg.event;

import net.tagucha.jrpg.JinroGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameStartEvent extends GameEvent{
    private static final HandlerList handlers = new HandlerList();

    public GameStartEvent(JinroGame game) {
        super(game);
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
