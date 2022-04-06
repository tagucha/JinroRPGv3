package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.tagucha.jrpg.JinroGame;
import net.tagucha.jrpg.PluginMain;

import java.util.Arrays;

public class EyeOfProvidence extends GameItem {
    public EyeOfProvidence(PluginMain plugin) {
        super(
                plugin,
                Material.SUNFLOWER,
                ChatColor.WHITE + "プロビデンスの眼光",
                Arrays.asList(
                        String.format("%s%s投げて使用",ChatColor.GREEN,ChatColor.ITALIC),
                        String.format("%s自分以外の全生存プレイヤーに",ChatColor.GRAY),
                        String.format("%s30秒間の発光効果%sをもたらす",ChatColor.WHITE,ChatColor.GRAY)
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("eye_of_providence", ItemType.SUPPORT)
        );
        this.setLighting(true);
    }

    @Override
    protected void onDrop(JinroGame game, PlayerDropItemEvent event) {
        game.getAlive().forEach(uuid -> {
            if (event.getPlayer().getUniqueId() != uuid) {
                this.plugin.getPlayer(uuid).ifPresent(player -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,30 * 20,1),true);
                });
            }
        });
        event.getItemDrop().remove();
    }
}
