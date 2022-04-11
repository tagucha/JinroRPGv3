package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import net.tagucha.jrpg.job.GameJob;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.UUID;

public class Telescope extends GameItem{
    public Telescope(JinroRPG plugin) {
        super(plugin,
                Material.SPYGLASS,
                ChatColor.WHITE + "望遠鏡",
                Arrays.asList(
                        String.format("%sなんの変哲もない望遠鏡に見えるが、", ChatColor.WHITE),
                        String.format("%s止めを刺した相手の看板に対して", ChatColor.WHITE),
                        String.format("%s検死官%sが使用すると人間かどうか判別できる", ChatColor.GOLD, ChatColor.WHITE)
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new GameItem.ConfigKey("telescope", ItemType.OTHER)
        );
    }

    @Override
    protected void onClickSign(JinroGame game, PlayerInteractEvent event, Player clicker, UUID target) {
        if (game.getJob(clicker.getUniqueId()).filter(job -> job.equals(GameJob.CORONER)).isEmpty()) {
            clicker.sendMessage(JinroRPG.getLogo(ChatColor.RED) + " 検死は検死官のみが行えます");
            return;
        }
        if (!this.plugin.JOB_MANAGER.isKilledByCorner(target)) {
            clicker.sendMessage(JinroRPG.getLogo(ChatColor.RED) + " 検死官が止め刺した相手のみ検死を行えます");
            return;
        }
        game.getJob(target).ifPresent(job -> clicker.sendMessage(String.format("%s %s%s", JinroRPG.getLogo(ChatColor.RED), plugin.getName(target), job.isHuman ? "は人間でした" : "は人間ではありませんでした")));
        event.setCancelled(true);
    }
}
