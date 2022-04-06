package net.tagucha.jrpg.exception;

import org.bukkit.ChatColor;

public class PermissionException extends SimpleMessageException {
    public PermissionException() {
        super(ChatColor.RED,ChatColor.RED,"権限がありません。");
    }
}
