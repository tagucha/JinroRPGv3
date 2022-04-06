package net.tagucha.jrpg.exception;

import net.tagucha.jrpg.JinroGame;

public class GameAreaInputException extends GameException{
    public GameAreaInputException(JinroGame game, String message) {
        super(game,message);
    }
}
