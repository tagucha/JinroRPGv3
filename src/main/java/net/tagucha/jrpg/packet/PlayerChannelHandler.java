package net.tagucha.jrpg.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.world.level.EnumGamemode;
import net.tagucha.jrpg.JinroGame;
import net.tagucha.jrpg.PluginMain;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class PlayerChannelHandler extends ChannelDuplexHandler {
    private final PluginMain plugin;
    private final Player player;

    public PlayerChannelHandler(PluginMain plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof PacketPlayOutPlayerInfo packet) {
            Optional<JinroGame> opt = this.plugin.isPlayer(this.player.getUniqueId());
            if (opt.isPresent()) {
                switch (packet.c()) {
                    case a:
                        if (!opt.get().isAlive(this.player.getUniqueId())) break;
                        if (!opt.get().getPlayers().contains(packet.b().get(0).a().getId())) break;
                        PacketPlayOutPlayerInfo.PlayerInfoData data = packet.b().get(0);
                        packet.b().clear();
                        packet.b().add(new PacketPlayOutPlayerInfo.PlayerInfoData(data.a(), data.b(), EnumGamemode.c, data.d()));
                        break;
                    case b:
                        if (UndarkCore.getCanceler().contains(this.player.getUniqueId())) {
                            packet.b().removeIf(d -> d.c() == EnumGamemode.d);
                            if (packet.b().isEmpty()) return;
                        }
                        break;
                }
            }
        }
        super.write(ctx, msg, promise);
    }
}
