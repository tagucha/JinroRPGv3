package net.tagucha.jrpg.menu;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Icon {
    private final ItemStack icon;

    public Icon(ItemStack icon) {
        this.icon = icon;
    }

    public Icon(Material material, String name, List<String> lore, int amount, int cmd, boolean enchant_effect) {
        icon = new ItemStack(material,amount);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_POTION_EFFECTS);
        if (enchant_effect) meta.addEnchant(Enchantment.ARROW_DAMAGE,1,true);
        if (cmd != -1) meta.setCustomModelData(cmd);
        icon.setItemMeta(meta);
    }

    public Icon(Material material,String name,List<String> lore) {
        this(material,name,lore,1,-1,false);
    }

    public Icon(Material material,String name,List<String> lore,boolean enchant_effect) {
        this(material,name,lore,1,-1,enchant_effect);
    }

    public Icon(Material material,String name,List<String> lore,int amount) {
        this(material,name,lore,amount,-1,false);
    }

    public Icon(ItemStack stack, Consumer<ItemStack> consumer) {
        this(stack);
        consumer.accept(this.icon);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public abstract void onClick(Player player);

    public static Icon createDummyIcon(Material material, int amount) {
        return new Icon(new ItemStack(material, amount)) {
            @Override
            public void onClick(Player player) {
            }
        };
    }

    public static Icon createDummyIcon(Material material, String name, int amount) {
        return new Icon(material, name, new ArrayList<>(), amount) {
            @Override
            public void onClick(Player player) {
            }
        };
    }
}
