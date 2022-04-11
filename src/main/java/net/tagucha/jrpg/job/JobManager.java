package net.tagucha.jrpg.job;

import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.event.GameChangeToDayEvent;
import net.tagucha.jrpg.event.GameChangeToNightEvent;
import net.tagucha.jrpg.event.GameEndEvent;
import net.tagucha.jrpg.event.GamePlayerKillPlayerEvent;
import net.tagucha.jrpg.item.items.Bread;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JobManager implements Listener {
    private final JinroRPG plugin;
    private final Set<UUID> coroner_killing = new HashSet<>();

    public JobManager(JinroRPG plugin) {
        this.plugin = plugin;
    }

    public boolean isKilledByCorner(UUID uuid) {
        return this.coroner_killing.contains(uuid);
    }

    @EventHandler
    public void onChangeToDay(GameChangeToDayEvent event) {
        if (event.getGame().isAlive(GameJob.BAKER)) {
            event.getGame().getAlive().forEach(uuid -> plugin.getPlayer(uuid).ifPresent(player -> player.getInventory().addItem(new Bread(this.plugin).getItemStack(1, event.getGame().timer.getDay()))));
        }
    }

    @EventHandler
    public void onChangeToNight(GameChangeToNightEvent event) {
        event.getGame().getWorkers(GameJob.SMART_WEREWOLF).stream().filter(event.getGame()::isAlive).forEach(uuid -> {
            event.getGame().getHeart().put(uuid, event.getGame().getHeart().getOrDefault(uuid, 0) + 1);
            event.getGame().sendMessage(uuid, JinroRPG.getLogo(ChatColor.RED) + " 残りの占い可能回数: " + event.getGame().getHeart().getOrDefault(uuid, 0) + "回");
        });
    }

    @EventHandler
    public void onKill(GamePlayerKillPlayerEvent event) {
        if (event.getGame().getJob(event.getKiller()).filter(job -> job.equals(GameJob.CORONER)).isPresent()) {
            event.getGame().sendMessage(event.getKiller(), String.format("%s %s%s", JinroRPG.getLogo(ChatColor.RED), plugin.getName(event.getTarget()), event.getGame().getJob(event.getTarget()).get().isHuman ? "は人間でした" : "は人間ではありませんでした"));
            coroner_killing.add(event.getTarget());
        }
    }

    @EventHandler
    public void onEndGame(GameEndEvent event) {
        this.coroner_killing.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity().removeScoreboardTag("sacred_cross")) return;
        if (this.plugin.isPlayer(event.getEntity().getUniqueId()).filter(game -> game.getJob(event.getEntity().getUniqueId()).filter(job -> job == GameJob.VAMPIRE).isPresent() && game.timer.isNight()).isPresent())
            event.setDamage(0);
    }
}
