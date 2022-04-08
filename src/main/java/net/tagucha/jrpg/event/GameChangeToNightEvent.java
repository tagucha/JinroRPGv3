package net.tagucha.jrpg.event;

import net.tagucha.jrpg.core.JinroGame;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameChangeToNightEvent extends GameEvent {
    private static final HandlerList handlers = new HandlerList();

    public GameChangeToNightEvent(JinroGame game) {
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
