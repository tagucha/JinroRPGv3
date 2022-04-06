package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.tagucha.jrpg.PluginMain;

import java.util.function.Consumer;

public class InvisiblePotion extends GameItem {
    public InvisiblePotion(PluginMain plugin) {
        super(
                plugin,
                Material.POTION,
                ChatColor.WHITE + "透明化のポーション",
                null,
                129,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("invisible_potion", ItemType.COMBAT)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            meta.setColor(Color.AQUA);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY,20 * 20,1),true);
            stack.setItemMeta(meta);
        };
    }
}
