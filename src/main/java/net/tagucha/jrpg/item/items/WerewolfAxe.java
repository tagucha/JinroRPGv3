package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.core.GameTimer;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.event.PlayerAttackEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import net.tagucha.jrpg.job.GameJob;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Consumer;

public class WerewolfAxe extends GameItem {
    public WerewolfAxe(PluginMain plugin) {
        super(
                plugin,
                Material.STONE_AXE,
                ChatColor.DARK_RED + "人狼の斧",
                Arrays.asList(
                        ChatColor.GRAY + "プレイヤーを一撃で倒せる",
                        ChatColor.GRAY + "昼のあいだは一度しか使えない",
                        ChatColor.RED + "※一回で壊れる",
                        ChatColor.RED + "※人狼以外購入できない"
                ),
                128,
                ItemPermission.WEREWOLF,
                TimePermission.ANYTIME,
                new ConfigKey("werewolf_axe", ItemType.COMBAT)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> stack.setDurability((short) 131);
    }

    @Override
    protected void onAttack(JinroGame game, PlayerAttackEvent event) {
        if (game.timer.getClock() == GameTimer.Clock.DAY && game.getAxeUsed().contains(event.getAttacker().getUniqueId())) {
            event.getAttacker().sendMessage(PluginMain.getLogo(ChatColor.RED) + " 昼のあいだは一度しか使えません");
            event.setCancelled(true);
            return;
        }
        if (!event.getTarget().getType().equals(EntityType.PLAYER)) {
            event.getBaseEvent().setDamage(1);
            return;
        }
        event.getBaseEvent().setDamage(100);
        event.getAttacker().getInventory().setItemInMainHand(null);
        event.getTarget().getWorld().playSound(event.getTarget().getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1);
        if(game.getJob(event.getAttacker().getUniqueId()).filter(job -> !job.equals(GameJob.INSANE_WEREWOLF)).isPresent()) game.getAxeUsed().add(event.getAttacker().getUniqueId());
    }
}
