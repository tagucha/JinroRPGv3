package net.tagucha.jrpg.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerAttackEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final Player attacker;
    private final LivingEntity target;
    private final EntityDamageByEntityEvent base_event;

    public PlayerAttackEvent(Player attacker, LivingEntity target, EntityDamageByEntityEvent base_event) {
        this.attacker = attacker;
        this.target = target;
        this.base_event = base_event;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getAttacker() {
        return attacker;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public EntityDamageByEntityEvent getBaseEvent() {
        return base_event;
    }
}
