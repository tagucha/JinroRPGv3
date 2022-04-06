package net.tagucha.jrpg.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HitStunGrenadeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player attacker;
    private final LivingEntity target;
    private final Snowball grenade;
    private boolean isCancelled = false;

    public HitStunGrenadeEvent(Player attacker, LivingEntity target, Snowball grenade) {
        this.attacker = attacker;
        this.target = target;
        this.grenade = grenade;
    }

    public Player getAttacker() {
        return attacker;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public Snowball getGrenade() {
        return grenade;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}