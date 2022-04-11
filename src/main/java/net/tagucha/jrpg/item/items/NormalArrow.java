package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import net.tagucha.jrpg.JinroRPG;

import java.util.Arrays;

public class NormalArrow extends GameItem {
    public NormalArrow(JinroRPG plugin) {
        super(
                plugin,
                Material.ARROW,
                null,
                Arrays.asList(String.format("%s弓を使う際、必要になる", ChatColor.GRAY),String.format("%s※一撃で無くなる",ChatColor.RED)),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("arrow", ItemType.COMBAT)
        );
    }
}
