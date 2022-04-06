package net.tagucha.jrpg.event;

import net.tagucha.jrpg.PluginMain;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class OriginalEventManager implements Listener {
    private final PluginMain plugin;

    public OriginalEventManager(PluginMain plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.plugin.getServer().getPluginManager().registerEvents(this,this.plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
            Player attacker = (Player) event.getDamager();
            LivingEntity target = (LivingEntity) event.getEntity();
            PlayerAttackEvent called_event = new PlayerAttackEvent(attacker,target,event);
            if (!called_event.isCancelled()) {
                this.plugin.getServer().getPluginManager().callEvent(called_event);
                event.setCancelled(called_event.isCancelled());
            }
        }
    }
}
