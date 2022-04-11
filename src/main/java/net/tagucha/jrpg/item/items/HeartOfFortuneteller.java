package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.event.GameTickEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;

import java.util.*;

public class HeartOfFortuneteller extends GameItem {
    private static Map<JinroGame, Map<UUID,Integer>> MAP = new HashMap<>();

    public HeartOfFortuneteller(JinroRPG plugin) {
        super(
                plugin,
                Material.HEART_OF_THE_SEA,
                ChatColor.DARK_PURPLE + "占い師の心",
                Arrays.asList(
                        ChatColor.GRAY + "購入した数だけ看板から役職を",
                        ChatColor.GRAY + "見ることができる",
                        ChatColor.RED + "※占いは一夜につき一度のみ"
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("heart_of_fortuneteller", ItemType.SUPPORT)
        );
        this.setLighting(true);
    }

    @EventHandler
    public void onTick(GameTickEvent event) {
        event.getGame().checkHeart();
    }
}
