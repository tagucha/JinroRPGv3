package net.tagucha.jrpg.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.world.level.EnumGamemode;
import net.tagucha.jrpg.core.JinroGame;
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
                        packet.b().set(0, new PacketPlayOutPlayerInfo.PlayerInfoData(packet.b().get(0).a(), packet.b().get(0).b(), EnumGamemode.c, packet.b().get(0).d()));
                        break;
                    case b:
                        if (UndarkCore.getCanceler().contains(this.player.getUniqueId())) {
                            for (int i = packet.b().size() - 1;i >= 0;i--) if (packet.b().get(i).c() == EnumGamemode.d) packet.b().remove(i);
                            if (packet.b().isEmpty()) return;
                        }
                        break;
                }
            }
        }
        super.write(ctx, msg, promise);
    }
}
