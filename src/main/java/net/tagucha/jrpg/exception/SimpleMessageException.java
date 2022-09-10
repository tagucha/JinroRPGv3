package net.tagucha.jrpg.exception;

import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;
import org.bukkit.ChatColor;

public class SimpleMessageException extends GameException {
    public SimpleMessageException(JinroGame game, ChatColor c1, ChatColor c2, String message) {
        super(game, String.format("%s%s%s", JinroRPG.getChatLogo(c1),c2,message));
    }

    public SimpleMessageException(ChatColor c1, ChatColor c2, String message) {
        this(null, c1, c2, message);
    }
}
