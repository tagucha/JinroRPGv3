package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Arrays;
import java.util.UUID;

public class AshOfMedium extends GameItem {
    public AshOfMedium(JinroRPG plugin) {
        super(
                plugin,
                Material.GUNPOWDER,
                ChatColor.WHITE + "霊媒師の遺灰",
                Arrays.asList(
                        String.format("%s%s投げて使用",ChatColor.GREEN,ChatColor.ITALIC),
                        String.format("%s死亡者全員の名前がわかる",ChatColor.GRAY)
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("ash_of_medium", ItemType.SUPPORT)
        );
        this.setLighting(true);
    }

    @Override
    protected void onDrop(JinroGame game, PlayerDropItemEvent event) {
        event.getPlayer().sendMessage(String.format("%s 死亡者 %d名", JinroRPG.getLogo(ChatColor.RED), game.getDead().size()));
        for (UUID uuid:game.getDead()) event.getPlayer().sendMessage(String.format("%s %s%s", JinroRPG.getLogo(ChatColor.RED), ChatColor.AQUA, this.plugin.getName(uuid)));
        event.getItemDrop().remove();
    }
}
