package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.Material;
import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class CookedBeef extends GameItem {
    public CookedBeef(JinroRPG plugin) {
        super(
                plugin,
                Material.COOKED_BEEF,
                null,
                null,
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("cooked_beef", ItemType.COMBAT)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return itemStack -> itemStack.setAmount(5);
    }
}
