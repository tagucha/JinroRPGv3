package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.event.GameTickEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.tagucha.jrpg.JinroGame;
import net.tagucha.jrpg.PluginMain;

import java.util.*;
import java.util.function.Consumer;

public class HeartOfFortuneteller extends GameItem {
    private static Map<JinroGame, Map<UUID,Integer>> MAP = new HashMap<>();

    public HeartOfFortuneteller(PluginMain plugin) {
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

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            ItemMeta meta = stack.getItemMeta();
            meta.addEnchant(Enchantment.ARROW_DAMAGE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        };
    }

    @EventHandler
    public void onTick(GameTickEvent event) {
        event.getGame().checkHeart();
    }
}
