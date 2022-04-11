package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;

import java.util.Arrays;

public class PrayOfKnight extends GameItem {
    public PrayOfKnight(JinroRPG plugin) {
        super(plugin,
                Material.ARMOR_STAND,
                ChatColor.WHITE + "騎士の祈り",
                Arrays.asList(
                        String.format("%s%sQボタンで使用 %s%s/ %s%s夜のみ使用可能",ChatColor.GREEN,ChatColor.ITALIC,ChatColor.RESET,ChatColor.ITALIC,ChatColor.BLUE,ChatColor.ITALIC),
                        String.format("%s夜の間に使うと%s日が昇るまで%s無敵になれる",ChatColor.GRAY,ChatColor.WHITE,ChatColor.GRAY),
                        String.format("%sただし%s大ダメージを受けると祈りは解ける",ChatColor.GRAY,ChatColor.WHITE),
                        String.format("%s※人狼は使用できない",ChatColor.RED)
                ),
                128,
                ItemPermission.NOT_WEREWOLF,
                TimePermission.NIGHT,
                new ConfigKey("pray_of_knight", ItemType.SUPPORT)
        );
    }

    @Override
    protected void onDrop(JinroGame game, PlayerDropItemEvent event) {
        if (game.pray(event.getPlayer().getUniqueId())) {
            if (event.getItemDrop().getItemStack().getAmount() == 1) event.getItemDrop().remove();
            else {
                event.setCancelled(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getItemDrop().getItemStack().getAmount() - 1);
                    }
                }.runTaskLater(this.plugin,1);
            }
            event.getPlayer().sendMessage(JinroRPG.getLogo(ChatColor.RED) + " 騎士の祈り を使用しました");
        } else {
            event.getPlayer().sendMessage(JinroRPG.getLogo(ChatColor.RED) + " 既に使用しています");
            event.setCancelled(true);
        }
    }
}
