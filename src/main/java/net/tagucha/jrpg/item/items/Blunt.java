package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.util.ItemUtil;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.item.ItemPermission;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class Blunt extends GameItem {
    public Blunt(JinroRPG plugin) {
        super(
                plugin,
                Material.STICK,
                ChatColor.WHITE + "鈍器",
                null,
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("blunt", ItemType.OTHER)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            if (this.plugin.getGameConfig().isReloaded()) if (this.plugin.getGameConfig().getCustomModelData().containsKey(this.getConfigKey().key()))
                ItemUtil.setCMD(stack, this.plugin.getGameConfig().getCustomModelData().get(this.getConfigKey().key()));
        };
    }
}
