package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.event.PlayerAttackEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import net.tagucha.jrpg.job.GameJob;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;

import java.util.Arrays;

public class SacredCross extends GameItem {
    public SacredCross(JinroRPG plugin) {
        super(
                plugin,
                Material.NETHER_STAR,
                ChatColor.WHITE + "聖なる十字架",
                Arrays.asList(
                        String.format("%s%s殴って使用",ChatColor.GREEN,ChatColor.ITALIC),
                        String.format("%s吸血鬼%sを一撃で倒せる",ChatColor.LIGHT_PURPLE,ChatColor.GRAY + "だった場合、倒すことができる"),
                        String.format("%s※一回で壊れる",ChatColor.RED)
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("sacred_cross", ItemType.SUPPORT)
        );
    }

    @Override
    protected void onAttack(JinroGame game, PlayerAttackEvent event) {
        if (event.getTarget() instanceof Player) {
            game.getJob(event.getTarget().getUniqueId()).ifPresent(job -> {
                if (job == GameJob.VAMPIRE) {
                    event.getBaseEvent().setDamage(100);
                }
                event.getAttacker().getInventory().getItemInMainHand().setAmount(event.getAttacker().getInventory().getItemInMainHand().getAmount() - 1);
            });
            event.getTarget().addScoreboardTag("sacred_cross");
        }
    }
}
