package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.function.Consumer;

public class SpeedPotion extends GameItem {
    public SpeedPotion(JinroRPG plugin) {
        super(
                plugin,
                Material.POTION,
                ChatColor.WHITE + "俊敏のポーション",
                Arrays.asList(
                        ChatColor.BLUE + "移動速度上昇（" + ChatColor.AQUA + "試合中永続" + ChatColor.BLUE + "）",
                        ChatColor.DARK_PURPLE + "効果:",
                        ChatColor.BLUE + "移動速上昇 +40%"
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("speed_potion", ItemType.SUPPORT)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            meta.setColor(Color.AQUA);
            meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED,100000000,1),true);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            stack.setItemMeta(meta);
        };
    }
}
