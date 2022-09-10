package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.event.GameUseEyeOfMadEvent;
import net.tagucha.jrpg.job.GameJob;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.JinroRPG;

import java.util.*;

public class EyeOfMad extends GameItem {
    public EyeOfMad(JinroRPG plugin) {
        super(
                plugin,
                Material.END_CRYSTAL,
                ChatColor.WHITE + "共犯者の目",
                Arrays.asList(ChatColor.GREEN + "" + ChatColor.ITALIC + "投げて使用",
                        ChatColor.DARK_RED + "人狼" + ChatColor.GRAY + "誰か一人の名前がわかる",
                        ChatColor.RED + "※共犯者以外使用できない"
                ),
                128,
                ItemPermission.MAD,
                TimePermission.ANYTIME,
                new ConfigKey("eye_of_mad", ItemType.SUPPORT)
        );
    }

    @Override
    protected void onDrop(JinroGame game, PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        List<UUID> list = new ArrayList<>(game.getWorkers(GameJob.WEREWOLF));
        Random random = new Random();
        for (int i = 0;i < event.getItemDrop().getItemStack().getAmount();i++) {
            UUID uuid = list.get(random.nextInt(list.size()));
            player.sendMessage(String.format("%s %s%s : %s",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    GameJob.WEREWOLF.getRealName(),
                    ChatColor.WHITE,
                    this.plugin.getName(uuid))
            );
            GameUseEyeOfMadEvent evt = new GameUseEyeOfMadEvent(game, player, uuid);
            Bukkit.getPluginManager().callEvent(evt);
        }
        event.getItemDrop().remove();
    }
}
