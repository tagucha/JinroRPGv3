package net.tagucha.jrpg.job;

import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.event.GameChangeToDayEvent;
import net.tagucha.jrpg.event.GameEndEvent;
import net.tagucha.jrpg.event.GamePlayerKillPlayerEvent;
import net.tagucha.jrpg.item.items.Bread;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JobManager implements Listener {
    private final PluginMain plugin;
    private final Set<UUID> coroner_killing = new HashSet<>();

    public JobManager(PluginMain plugin) {
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
    public void onKill(GamePlayerKillPlayerEvent event) {
        if (event.getGame().getJob(event.getKiller()).filter(job -> job.equals(GameJob.CORONER)).isPresent()) {
            event.getGame().sendMessage(event.getKiller(), String.format("%s %s%s", PluginMain.getLogo(ChatColor.RED), plugin.getName(event.getTarget()), event.getGame().getJob(event.getTarget()).get().isHuman ? "は人間でした" : "は人間ではありませんでした"));
            coroner_killing.add(event.getTarget());
        }
    }

    @EventHandler
    public void onEndGame(GameEndEvent event) {
        this.coroner_killing.clear();
    }
}
