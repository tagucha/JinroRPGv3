package net.tagucha.jrpg.exception;

import net.tagucha.jrpg.core.JinroGame;
import org.bukkit.ChatColor;

public class PopulationOverException extends SimpleMessageException {
    public PopulationOverException(JinroGame game, int population) {
        super(game,ChatColor.RED,ChatColor.RED,String.format("%d人でゲームは開始できません。",population));
    }
}
