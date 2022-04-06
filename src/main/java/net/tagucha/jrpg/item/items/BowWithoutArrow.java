package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;

import java.util.Collections;
import java.util.function.Consumer;

public class BowWithoutArrow extends GameItem {
    public BowWithoutArrow(PluginMain plugin) {
        super(
                plugin,
                Material.BOW,
                ChatColor.AQUA + "Bow without arrows",
                Collections.singletonList(ChatColor.RED + "※一回で壊れる"),
                129,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("bow_without_arrow", ItemType.OTHER)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            ItemMeta meta = stack.getItemMeta();
            meta.addEnchant(Enchantment.ARROW_DAMAGE,1000,true);
            meta.addEnchant(Enchantment.ARROW_INFINITE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_ATTRIBUTES);
            if (this.plugin.getGameConfig().isReloaded()) if (this.plugin.getGameConfig().getCustomModelData().containsKey(this.getConfigKey().key))
                meta.setCustomModelData(this.plugin.getGameConfig().getCustomModelData().get(this.getConfigKey().key));
            stack.setItemMeta(meta);
            stack.setDurability((short) 384);
        };
    }


}
