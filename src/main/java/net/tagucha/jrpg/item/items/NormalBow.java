package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;

import java.util.Collections;
import java.util.function.Consumer;

public class NormalBow extends GameItem {
    public NormalBow(PluginMain plugin) {
        super(
                plugin,
                Material.BOW,
                ChatColor.AQUA + "Bow",
                Collections.singletonList(ChatColor.RED + "※一回で壊れる"),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("bow", ItemType.COMBAT)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            ItemMeta meta = stack.getItemMeta();
            meta.addEnchant(Enchantment.ARROW_DAMAGE,1000,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
            stack.setDurability((short) 384);
        };
    }

    @EventHandler
    public void onStartBow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInHand().isSimilar(this.plugin.ITEMS.NORMAL_BOW)) {
            if (player.getInventory().contains(this.plugin.ITEMS.NORMAL_ARROW)) return;
            event.setCancelled(true);
        }
    }
}
