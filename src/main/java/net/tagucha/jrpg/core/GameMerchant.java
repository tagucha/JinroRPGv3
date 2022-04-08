package net.tagucha.jrpg.core;

import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.item.GameItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public record GameMerchant(PluginMain plugin) {
    public Villager spawnCombatMerchant(Location location) {
        Villager villager = spawnSimpleMerchant(location);
        villager.setCustomName(ChatColor.GREEN + "戦闘");
        villager.setProfession(Villager.Profession.FARMER);
        villager.setRecipes(new ArrayList<>(this.plugin.ITEMS.merchant_recipes.get(GameItem.ItemType.COMBAT).values()));
        return villager;
    }

    public Villager spawnSupportMerchant(Location location) {
        Villager villager = spawnSimpleMerchant(location);
        villager.setCustomName(ChatColor.GREEN + "補助");
        villager.setProfession(Villager.Profession.LIBRARIAN);
        villager.setRecipes(new ArrayList<>(this.plugin.ITEMS.merchant_recipes.get(GameItem.ItemType.SUPPORT).values()));
        return villager;
    }

    private Villager spawnSimpleMerchant(Location location) {
        World world = location.getWorld();
        Location loc = new Location(world, (double) location.getBlockX() + 0.5D, location.getBlockY(), (double) location.getBlockZ() + 0.5D, rotation(location.getYaw(), 45.0D), 0.0F);
        Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        villager.setGravity(false);
        villager.setInvulnerable(true);
        villager.setAI(false);
        villager.setSilent(true);
        villager.setVillagerLevel(5);
        return villager;
    }

    public static boolean isMerchant(Entity entity) {
        if (entity.getCustomName() == null) return false;
        return entity.getType() == EntityType.VILLAGER && (entity.getCustomName().equalsIgnoreCase(ChatColor.GREEN + "補助") || entity.getCustomName().equalsIgnoreCase(ChatColor.GREEN + "戦闘"));
    }

    public static boolean isSupportMerchant(Entity entity) {
        if (entity.getCustomName() == null) return false;
        return entity.getType() == EntityType.VILLAGER && entity.getCustomName().equalsIgnoreCase(ChatColor.GREEN + "補助");
    }

    public static boolean isCombatMerchant(Entity entity) {
        if (entity.getCustomName() == null) return false;
        return entity.getType() == EntityType.VILLAGER && entity.getCustomName().equalsIgnoreCase(ChatColor.GREEN + "戦闘");
    }

    public void respawn(World world) {
        world.getLivingEntities().stream().filter(entity -> entity.getType().equals(EntityType.VILLAGER)).forEach(entity -> {
            if (entity.getCustomName() != null) {
                if (isSupportMerchant(entity)) new BukkitRunnable() {
                    @Override
                    public void run() {
                        spawnSupportMerchant(entity.getLocation());
                        entity.remove();
                    }
                }.runTaskLater(plugin, 1);
                else if (isCombatMerchant(entity)) new BukkitRunnable() {
                    @Override
                    public void run() {
                        spawnCombatMerchant(entity.getLocation());
                        entity.remove();
                    }
                }.runTaskLater(plugin, 1);
            }
        });
    }

    public static float rotation(double r, double p) {
        while (r < 0.0D) {
            r += 360.0D;
        }

        while (r > 360.0D) {
            r -= 360.0D;
        }

        return ((float) ((int) (r / p)) + (float) (r % 45.0D > p / 2.0D ? 1 : 0)) * 45.0F;
    }
}
