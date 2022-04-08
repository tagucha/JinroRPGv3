package net.tagucha.jrpg.item;

import net.tagucha.jrpg.core.GameManager;
import net.tagucha.jrpg.event.PlayerAttackEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.tagucha.jrpg.core.JinroGame;
import net.tagucha.jrpg.PluginMain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class GameItem implements Listener {
    protected final PluginMain plugin;
    protected final Material material;
    protected final String name;
    protected final List<String> lore;
    protected final int custom_model_data;
    protected final ItemPermission item_perm;
    protected final TimePermission time_perm;
    public final ConfigKey config_key;
    private boolean isLighting = false;
    private int default_amount = 1;

    public GameItem(PluginMain plugin, Material material, String name, List<String> lore, int custom_model_data, ItemPermission item_perm, TimePermission time_perm, ConfigKey config_key) {
        this.plugin = plugin;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.custom_model_data = custom_model_data;
        this.item_perm = item_perm;
        this.time_perm = time_perm;
        this.config_key = config_key;
    }

    public ItemStack getItemStack(int amount,Object... objects) {
        ItemStack stack = new ItemStack(material,amount);
        ItemMeta meta = stack.getItemMeta();
        if (name != null) meta.setDisplayName(this.name);
        if (lore != null) meta.setLore(this.lore);
        if (isLighting) {
            meta.addEnchant(Enchantment.ARROW_DAMAGE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setCustomModelData(this.custom_model_data);
        stack.setItemMeta(meta);
        Consumer<ItemStack> consumer = toUpdateItem(objects);
        if (consumer != null) consumer.accept(stack);
        return stack;
    }

    public void setLighting(boolean lighting) {
        isLighting = lighting;
    }

    public Consumer<ItemStack> toUpdateItem(Object... objects){
        return null;
    }

    public ItemStack register() {
        this.plugin.getServer().getPluginManager().registerEvents(this,this.plugin);
        return getItemStack(this.default_amount);
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getCustomModelData() {
        return custom_model_data;
    }

    public int getDefaultAmount() {
        return default_amount;
    }

    public void setDefaultAmount(int default_amount) {
        this.default_amount = default_amount;
    }

    public ConfigKey getConfigKey() {
        return config_key;
    }

    protected void onDrop(JinroGame game, PlayerDropItemEvent event){
    }

    protected void onAttack(JinroGame game, PlayerAttackEvent event) {
    }

    protected void onDamage(JinroGame game, EntityDamageEvent event) {
    }

    protected void onClickSign(JinroGame game, PlayerInteractEvent event, Player clicker, UUID target) {
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onDropEvent(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (this.plugin.ITEMS.getItem(this).isSimilar(event.getItemDrop().getItemStack())) {
            Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
            if (optional.isPresent()) {
                if (!this.item_perm.canUse(optional.get().getJob(player.getUniqueId()).get())) {
                    player.sendMessage(String.format(this.item_perm.getErrorMessage(), this.name));
                    event.setCancelled(true);
                    return;
                } else if (!this.time_perm.canUse(optional.get().timer.getClock())) {
                    player.sendMessage(String.format(this.time_perm.getErrorMessage(), this.name));
                    event.setCancelled(true);
                    return;
                }
                this.onDrop(optional.get(), event);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAttackEvent(PlayerAttackEvent event) {
        final Player player = event.getAttacker();
        if (this.plugin.ITEMS.getItem(this).isSimilar(player.getInventory().getItemInMainHand())) {
            Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
            if (optional.isPresent()) {
                if (!this.item_perm.canUse(optional.get().getJob(player.getUniqueId()).get())) {
                    player.sendMessage(String.format(this.item_perm.getErrorMessage(), this.name));
                    event.setCancelled(true);
                    return;
                } else if (!this.time_perm.canUse(optional.get().timer.getClock())) {
                    player.sendMessage(String.format(this.time_perm.getErrorMessage(), this.name));
                    event.setCancelled(true);
                    return;
                }
                this.onAttack(optional.get(), event);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
            optional.ifPresent(game -> this.onDamage(game, event));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickingSignEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.ITEMS.getItem(this).isSimilar(player.getInventory().getItemInMainHand())) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            Block block = event.getClickedBlock();
            if (GameManager.isSign(block.getType())) {
                Sign sign = (Sign) block.getState();
                Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
                if (optional.isPresent()) {
                    JinroGame game = optional.get();
                    for (UUID uuid : game.getPlayers()) {
                        if (!(sign.getLine(1).equalsIgnoreCase("§1§l§n" + this.plugin.getName(uuid)) && sign.getLine(3).equalsIgnoreCase(GameManager.TO_FORTUNE)))
                            continue;
                        if (!this.item_perm.canUse(optional.get().getJob(player.getUniqueId()).get())) {
                            player.sendMessage(String.format(this.item_perm.getErrorMessage(), this.name));
                            event.setCancelled(true);
                            return;
                        } else if (!this.time_perm.canUse(optional.get().timer.getClock())) {
                            player.sendMessage(String.format(this.time_perm.getErrorMessage(), this.name));
                            event.setCancelled(true);
                            return;
                        }
                        this.onClickSign(game, event, player, uuid);
                    }
                }
            }
        }
    }

    public static class ConfigKey {
        public final String key;
        public final ItemType type;

        public ConfigKey(String key, ItemType type) {
            this.key = key;
            this.type = type;
        }
    }

    public enum ItemType {
        COMBAT("combat", "武器"),
        SUPPORT("support", "補助"),
        OTHER("other", "その他");

        public final String key;
        public final String jp;

        ItemType(String key, String jp) {
            this.key = key;
            this.jp = jp;
        }
    }
}
