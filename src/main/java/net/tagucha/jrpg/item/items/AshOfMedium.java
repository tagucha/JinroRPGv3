package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

public class AshOfMedium extends GameItem {
    public AshOfMedium(PluginMain plugin) {
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
        event.getPlayer().sendMessage(String.format("%s 死亡者 %d名", PluginMain.getLogo(ChatColor.RED), game.getDead().size()));
        for (UUID uuid:game.getDead()) event.getPlayer().sendMessage(String.format("%s %s%s", PluginMain.getLogo(ChatColor.RED), ChatColor.AQUA, this.plugin.getName(uuid)));
        event.getItemDrop().remove();
    }
}
