package net.tagucha.jrpg.item;

import net.tagucha.jrpg.item.items.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import net.tagucha.jrpg.PluginMain;

import java.util.*;

public class GameItems {
    public final Map<GameItem, ItemStack> match = new HashMap<>();
    public final HashSet<GameItem> items = new HashSet<>();
    public final Map<GameItem.ItemType, Map<String, MerchantRecipe>> merchant_recipes = new HashMap<>();

    public final ItemStack BLUNT;

    public final ItemStack NORMAL_BOW;
    public final ItemStack NORMAL_ARROW;
    public final ItemStack COOKED_BEEF;
    public final ItemStack[] SKELETON_KILLER = new ItemStack[31];
    public final ItemStack STUN_GRENADE;
    public final ItemStack WEREWOLF_AXE;
    public final ItemStack INVISIBLE_POTION;
    public final ItemStack TRIDENT_OF_RANCOR;

    public final ItemStack SPEED_POTION;
    public final ItemStack HEART_OF_FORTUNE;
    public final ItemStack PRAY_OF_NIGHT;
    public final ItemStack EYE_OF_MAD;
    public final ItemStack SACRED_CROSS;
    public final ItemStack EYE_OF_PROVIDENCE;
    public final ItemStack TALISMAN;
    public final ItemStack PROVIDENCE_OF_KNIGHT;
    public final ItemStack ASH_OF_MEDIUM;


    private final PluginMain plugin;

    public GameItems(PluginMain plugin) {
        this.plugin = plugin;
        for (GameItem.ItemType type : GameItem.ItemType.values()) this.merchant_recipes.put(type, new HashMap<>());

        this.BLUNT = this.register(new Blunt(this.plugin), 0);

        this.NORMAL_BOW = this.register(new NormalBow(this.plugin),2);
        this.NORMAL_ARROW = this.register(new NormalArrow(this.plugin),2);
        this.COOKED_BEEF = this.register(new CookedBeef(this.plugin),1);
        this.SKELETON_KILLER[0] = new ItemStack(Material.AIR);
        for (int i = 1;i < 30;i++) this.SKELETON_KILLER[i] = this.register(new SkeletonKiller(this.plugin, i),0);
        this.SKELETON_KILLER[30] = this.register(new SkeletonKiller(this.plugin, 30),4);
        this.STUN_GRENADE = this.register(new StunGrenade(this.plugin),2);
        this.WEREWOLF_AXE = this.register(new WerewolfAxe(this.plugin),4);
        this.INVISIBLE_POTION = this.register(new InvisiblePotion(this.plugin),4);
        this.TRIDENT_OF_RANCOR = this.register(new TridentOfRancor(this.plugin), 3);

        this.SPEED_POTION = this.register(new SpeedPotion(this.plugin),1);
        this.HEART_OF_FORTUNE = this.register(new HeartOfFortuneteller(this.plugin),5);
        this.PRAY_OF_NIGHT = this.register(new PrayOfKnight(this.plugin),3);
        this.EYE_OF_MAD = this.register(new EyeOfMad(this.plugin),3);
        this.SACRED_CROSS = this.register(new SacredCross(this.plugin),2);
        this.EYE_OF_PROVIDENCE = this.register(new EyeOfProvidence(this.plugin),3);
        this.TALISMAN = this.register(new Talisman(this.plugin),1);
        this.PROVIDENCE_OF_KNIGHT = this.register(new ProvidenceOfKnight(this.plugin), 2);
        this.ASH_OF_MEDIUM = this.register(new AshOfMedium(this.plugin), 4);
    }

/*
    public void reset() {
        this.merchant_recipes.clear();
        for (GameItem.ItemType type : GameItem.ItemType.values()) this.merchant_recipes.put(type, new HashMap<>());
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("bow"),2,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("arrow"),2,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("cooked_beef"),1,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("skeleton_killer"),4,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("stan_grenade"),2,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("werewolf_axe"),4,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("speed_potion"),1,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("invisible_potion"),4,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.COMBAT).get("heart_of_fortuneteller"),4,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.SUPPORT).get("pray_of_knight"),3,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.SUPPORT).get("eye_of_mad"),4,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.SUPPORT).get("sacred_cross"),2,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.SUPPORT).get("eye_of_providence"),2,128);
        ItemUtil.setPriceAndCMD(this.merchant_recipes.get(GameItem.ItemType.SUPPORT).get("talisman"),2,128);
    }

 */

    public ItemStack register(GameItem item, int emerald) {
        ItemStack stack = item.register();
        if (emerald > 0) {
            MerchantRecipe recipe = new MerchantRecipe(stack, Integer.MAX_VALUE);
            recipe.setIngredients(Collections.singletonList(new ItemStack(Material.EMERALD, emerald)));
            recipe.setExperienceReward(false);
            this.merchant_recipes.get(item.getConfigKey().type).put(item.getConfigKey().key, recipe);
            this.items.add(item);
        }
        this.match.put(item, stack);
        return stack;
    }

    public ItemStack getItem(GameItem item) {
        return this.match.getOrDefault(item, new ItemStack(Material.AIR));
    }
}
