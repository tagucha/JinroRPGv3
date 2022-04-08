package net.tagucha.jrpg.event;

import net.tagucha.jrpg.PluginMain;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public record OriginalEventManager(PluginMain plugin) implements Listener {
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity target) {
            PlayerAttackEvent called_event = new PlayerAttackEvent(attacker, target, event);
            if (!called_event.isCancelled()) {
                this.plugin.getServer().getPluginManager().callEvent(called_event);
                event.setCancelled(called_event.isCancelled());
            }
        }
    }
}
