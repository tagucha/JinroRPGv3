package net.tagucha.jrpg.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ItemUtil {
    public static void setPriceAndCMD(MerchantRecipe recipe, int price, int cmd) {
        recipe.setIngredients(Collections.singletonList(new ItemStack(Material.EMERALD, price)));
        setCMD(recipe.getResult(), cmd);
    }

    public static void setCMD(ItemStack item, int cmd) {
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(cmd);
        item.setItemMeta(meta);
    }
}
