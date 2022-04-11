package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.UUID;

public class ProvidenceOfKnight extends GameItem {
    public ProvidenceOfKnight(JinroRPG plugin) {
        super(plugin,
                Material.GOLDEN_HORSE_ARMOR,
                ChatColor.WHITE + "騎士の加護",
                Arrays.asList(
                        String.format("%s%s看板に使用 %s%s/ %s%s夜のみ使用可能",ChatColor.GREEN,ChatColor.ITALIC,ChatColor.RESET,ChatColor.ITALIC,ChatColor.BLUE,ChatColor.ITALIC),
                        String.format("%s対象を日が昇るまで%s一度だけ%s致命傷",ChatColor.GRAY,ChatColor.WHITE,ChatColor.GRAY),
                        String.format("%sから護ることができる",ChatColor.GRAY),
                        String.format("%s護衛が成功すると%s翌朝に通知%sが届く",ChatColor.GRAY,ChatColor.WHITE,ChatColor.GRAY),
                        String.format("%s※自分には使用できない",ChatColor.RED),
                        String.format("%s※人狼にも使用できるが効果はない",ChatColor.RED)
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.NIGHT,
                new ConfigKey("providence_of_knight", ItemType.SUPPORT)
        );
    }

    @Override
    protected void onClickSign(JinroGame game, PlayerInteractEvent event, Player clicker, UUID target) {
        if (clicker.getUniqueId().equals(target)) {
            clicker.sendMessage(JinroRPG.getLogo(ChatColor.RED) + " 自分には使用できません");
        } else {
            this.plugin.getPlayer(target).ifPresent(player -> {
                if (game.pray(target)) {
                    clicker.sendMessage(String.format("%s %s に 騎士の加護 を使用しました", JinroRPG.getLogo(ChatColor.RED), player.getName()));
                    clicker.getInventory().setItemInMainHand(null);
                } else event.getPlayer().sendMessage(JinroRPG.getLogo(ChatColor.RED) + " 既に使用されています");
            });
        }
        event.setCancelled(true);
    }
}
