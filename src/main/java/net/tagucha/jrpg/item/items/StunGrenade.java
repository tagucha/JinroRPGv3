package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.event.HitStunGrenadeEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Random;

public class StunGrenade extends GameItem {
    public StunGrenade(PluginMain plugin) {
        super(
                plugin,
                Material.SNOWBALL,
                ChatColor.WHITE + "スタングレネード",
                Arrays.asList(
                        ChatColor.GRAY + "当てた対象を" + ChatColor.WHITE + "5秒間" + ChatColor.GRAY + "盲目にし",
                        ChatColor.GRAY + "行動不能にする"
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("stan_grenade", ItemType.COMBAT)
        );
    }

    @EventHandler
    public void onThroughGrenade(ProjectileLaunchEvent event) {
        ProjectileSource source = event.getEntity().getShooter();
        if (source instanceof Player) {
            if (((Player) source).getInventory().getItemInMainHand().isSimilar(this.plugin.ITEMS.STUN_GRENADE)) {
                event.getEntity().setMetadata("grenade",new FixedMetadataValue(this.plugin,true));
            }
        }
    }

    @EventHandler
    public void onHitGrenade(ProjectileHitEvent event) {
        event.getEntity().getMetadata("grenade").forEach(value ->  {
            if(value.getOwningPlugin() == this.plugin && value.asBoolean()) {
                if (event.getHitEntity() instanceof LivingEntity && event.getEntity() instanceof Snowball) {
                    Player attacker = (Player) event.getEntity().getShooter();
                    LivingEntity target = (LivingEntity) event.getHitEntity();
                    Snowball grenade = (Snowball) event.getEntity();
                    HitStunGrenadeEvent evt = new HitStunGrenadeEvent(attacker,target,grenade);
                    if (!evt.isCancelled()) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,100,2),false);
                        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,100,128),false);
                        if (target instanceof Player) {
                            Player player = (Player) target;
                            player.setWalkSpeed(0.0F);
                            new BukkitRunnable() {
                                public void run() {
                                    player.setWalkSpeed(0.2F);
                                }
                            }.runTaskLater(this.plugin, 100L);
                        } else {
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,100,255),false);
                        }
                        Random random = new Random();
                        for(int i = 0; i < 8; ++i) {
                            target.getWorld().spawnParticle(Particle.CLOUD, target.getEyeLocation(), 0, (double)random.nextFloat() - 0.5D, (double)random.nextFloat() - 0.5D, (double)random.nextFloat() - 0.5D);
                        }
                    }
                }
            }
        });
    }
}
