package net.tagucha.jrpg.exception;

import net.tagucha.jrpg.JinroGame;

public class GameException extends Exception {
    private final JinroGame game;

    public GameException(JinroGame game,String message) {
        super(message);
        this.game = game;
    }
}
