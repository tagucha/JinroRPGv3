package net.tagucha.jrpg.packet;

import io.netty.channel.Channel;
import net.tagucha.jrpg.PluginMain;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public final class NettyInjector implements Listener {
    private final PluginMain plugin;
    private final HashMap<PlayerChannelHandler, Player> connection = new HashMap<>();

    public NettyInjector(PluginMain plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void inject(Player player) {
        if (!this.plugin.isEnabled()) return;
        Channel channel = ((CraftPlayer)player).getHandle().b.a.k;
        PlayerChannelHandler pch = new PlayerChannelHandler(this.plugin, player);
        if (channel.pipeline().get(PlayerChannelHandler.class) == null) {
            if (channel.pipeline().toMap().containsKey(this.plugin.getName())) {
                channel.pipeline().remove(channel.pipeline().toMap().get(this.plugin.getName()));
            }

            try {
                channel.pipeline().addBefore("packet_handler", this.plugin.getName(), pch);
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            this.connection.put(pch, player);
        }
    }

    public void remove(Player player) {
        if (!this.plugin.isEnabled()) return;
        Channel channel = ((CraftPlayer)player).getHandle().b.a.k;
        if (channel.pipeline().get(PlayerChannelHandler.class) != null) {
            channel.pipeline().remove(PlayerChannelHandler.class);

            for (PlayerChannelHandler handler : this.connection.keySet()) {
                if (this.connection.get(handler).equals(player)) {
                    this.connection.remove(handler);
                }
            }
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        this.connection.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        this.inject(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        this.remove(e.getPlayer());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.inject(player);
        }
    }
}
