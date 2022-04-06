package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.tagucha.jrpg.JinroGame;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.event.PlayerAttackEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;

import java.util.Arrays;
import java.util.function.Consumer;

public class SkeletonKiller extends GameItem {
    private final int lvl;

    public SkeletonKiller(PluginMain plugin, int lvl) {
        super(
                plugin,
                Material.WOODEN_SWORD,
                String.format("%sスケ狩り剣 %s(%d/30)",ChatColor.WHITE,ChatColor.GRAY,lvl),
                Arrays.asList(
                        ChatColor.GRAY + "スケルトンを一撃で倒せる。",
                        ChatColor.GRAY + "ただし30回しか使えない。"
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("skeleton_killer", ItemType.COMBAT)
        );
        this.lvl = lvl;
    }

    public int getLevel() {
        return this.lvl;
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            ItemMeta meta = stack.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);
            stack.setItemMeta(meta);
        };
    }

    @Override
    protected void onAttack(JinroGame game, PlayerAttackEvent event) {
        event.getAttacker().getInventory().setItemInMainHand(plugin.ITEMS.SKELETON_KILLER[this.lvl - 1]);
        event.getBaseEvent().setDamage(100);
    }
}
