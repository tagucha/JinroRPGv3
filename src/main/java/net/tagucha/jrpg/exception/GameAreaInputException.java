package net.tagucha.jrpg.exception;

import net.tagucha.jrpg.core.JinroGame;

public class GameAreaInputException extends GameException{
    public GameAreaInputException(JinroGame game, String message) {
        super(game,message);
    }
}
