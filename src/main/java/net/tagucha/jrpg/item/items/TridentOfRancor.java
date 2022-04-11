package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.event.PlayerAttackEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class TridentOfRancor extends GameItem {
    private static final Set<UUID> flying_tridents = new HashSet<>();

    public TridentOfRancor(JinroRPG plugin) {
        super(
                plugin,
                Material.TRIDENT,
                ChatColor.WHITE + "怨念の槍",
                Arrays.asList(
                        ChatColor.GRAY + "プレイヤーを２発で倒せる",
                        ChatColor.GRAY + "外した場合は返ってくる",
                        ChatColor.RED + "※一回で壊れる"
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("trident_of_rancor", ItemType.COMBAT)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            ItemMeta meta = stack.getItemMeta();
            meta.addEnchant(Enchantment.LOYALTY, 3, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            stack.setItemMeta(meta);
        };
    }

    @Override
    protected void onAttack(JinroGame game, PlayerAttackEvent event) {
        event.getBaseEvent().setDamage(1);
    }

    @EventHandler
    public void onLaunchTrident(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player) {
            if (this.plugin.isPlayer(player.getUniqueId()).isEmpty()) return;
            if (event.getEntity() instanceof Trident trident) {
                if (!trident.getItem().isSimilar(this.plugin.ITEMS.TRIDENT_OF_RANCOR)) return;
                flying_tridents.add(trident.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onHitTrident(ProjectileHitEvent event) {
        if (event.getHitEntity() == null) {
            flying_tridents.remove(event.getEntity().getUniqueId());
            return;
        }
        if (event.getHitEntity().getType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity().getType() != EntityType.TRIDENT) return;
        Trident trident = (Trident) event.getEntity();
        if (!trident.getItem().isSimilar(this.plugin.ITEMS.TRIDENT_OF_RANCOR)) return;
        this.plugin.isPlayer(event.getHitEntity().getUniqueId()).ifPresent(game -> {
            if (game.getCursed().contains(event.getHitEntity().getUniqueId())) ((Player) event.getHitEntity()).damage(100, ((Player) trident.getShooter()));
            else {
                game.getCursed().add(event.getHitEntity().getUniqueId());
                ((Player) event.getHitEntity()).damage(0, ((Player) trident.getShooter()));
            }
            flying_tridents.remove(trident.getUniqueId());
            trident.remove();
            trident.getWorld().spawn(trident.getLocation(), Trident.class, trd -> {
                trd.setVelocity(trident.getVelocity());
                trd.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                flying_tridents.add(trd.getUniqueId());
            });
        });
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (flying_tridents.contains(event.getDamager().getUniqueId())) {
            event.setCancelled(true);
            flying_tridents.remove(event.getDamager().getUniqueId());
        }
    }
}
