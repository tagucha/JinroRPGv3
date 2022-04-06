package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.util.ItemUtil;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class DefaultArrow extends GameItem {
    public DefaultArrow(PluginMain plugin) {
        super(
                plugin,
                Material.ARROW,
                ChatColor.WHITE + "Don't Move This Item!",
                null,
                129,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("default_arrow", ItemType.OTHER)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            if (this.plugin.getGameConfig().isReloaded()) if (this.plugin.getGameConfig().getCustomModelData().containsKey(this.getConfigKey().key))
                ItemUtil.setCMD(stack, this.plugin.getGameConfig().getCustomModelData().get(this.getConfigKey().key));
        };
    }
}
