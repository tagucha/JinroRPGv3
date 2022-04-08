package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.util.ItemUtil;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

public class Bread extends GameItem {
    public static final String[] names = new String[]{
            "あんパン", "マフィン", "食パン", "カレーパン",
            "クリームパン", "クロワッサン", "コッペパン", "コロネ",
            "ジャムパン", "シナモンロール", "スコーン", "ドーナツ",
            "ナン", "フランスパン", "バタール", "パン・オ・ショコラ",
            "メロンパン", "ブリパッシュ", "ブレッチェン", "カイザーゼンメル"
    };

    public Bread(PluginMain plugin) {
        super(
                plugin,
                Material.BREAD,
                String.format("%s[%s日目]焼き立ての%s", ChatColor.GOLD,"%d","%s"),
                Arrays.asList(
                        String.format("%sパン屋さんが生存していたら",ChatColor.WHITE),
                        String.format("%s毎日生存者に配られるパン",ChatColor.WHITE),
                        String.format("%s※食用",ChatColor.GREEN)
                ),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("bread", ItemType.OTHER)
        );
    }

    @Override
    public Consumer<ItemStack> toUpdateItem(Object... objects) {
        return stack -> {
            if (!(objects[0] instanceof Integer)) stack.setType(Material.AIR);
            else {
                Random random = new Random();
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(String.format(this.name,objects[0],names[random.nextInt(names.length)]));
                stack.setItemMeta(meta);
            }
        };
    }
}
