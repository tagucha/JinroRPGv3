package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;

import java.util.Arrays;

public class Talisman extends GameItem {
    public Talisman(JinroRPG plugin) {
        super(
                plugin,
                Material.PAPER,
                ChatColor.WHITE + "天啓の呪符",
                Arrays.asList(
                        String.format("%s%sQボタンで使用",ChatColor.GREEN,ChatColor.ITALIC),
                        String.format("%s次の朝が来るまで%s占われたこと",ChatColor.GRAY,ChatColor.WHITE),
                        String.format("%sを察知できるようになる。",ChatColor.GRAY)
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.NIGHT,
                new ConfigKey("talisman", ItemType.SUPPORT)
        );
        this.setLighting(true);
    }

    @Override
    protected void onDrop(JinroGame game, PlayerDropItemEvent event) {
        if (game.useTalisman(event.getPlayer().getUniqueId())) event.getItemDrop().getItemStack().setAmount(event.getItemDrop().getItemStack().getAmount() - 1);
    }
}
