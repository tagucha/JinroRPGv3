package net.tagucha.jrpg.event;

import net.tagucha.jrpg.core.JinroGame;
import org.bukkit.event.Event;

public abstract class GameEvent extends Event {
    private final JinroGame game;

    public GameEvent(JinroGame game) {
        this.game = game;
    }

    public JinroGame getGame() {
        return game;
    }
}
