package net.tagucha.jrpg.menu;

import net.tagucha.jrpg.PluginMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public class Menu implements Listener {
    public static final ItemStack NULL;

    private final PluginMain plugin;
    private String title;
    private int size;
    private final Map<Integer,Icon> map;
    private final List<Inventory> inventories = new ArrayList<>();
    private ItemStack menu_item = null;

    static {
        NULL = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = NULL.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        NULL.setItemMeta(meta);
    }

    public Menu(PluginMain plugin,String title, int size, Map<Integer,Icon> map) {
        this.plugin = plugin;
        this.title = title;
        this.size = cast(size);
        this.map = map;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    public Menu(PluginMain plugin,String title,int size) {
        this(plugin,title,size,new HashMap<>());
    }

    public void add(int i,Icon icon) throws IndexOutOfBoundsException{
        if (i  < 0 || size <= i) throw new IndexOutOfBoundsException("メニューの大きさ越えてんだけどwww");
        map.remove(i);
        map.put(i,icon);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSize(int size) {
        this.size = cast(size);
    }

    protected List<Inventory> getInventories() {
        return this.inventories;
    }

    public void setMenuItem(ItemStack stack) {
        this.menu_item = stack;
    }

    public void setMenuItem(Material material, Consumer<ItemStack> consumer) {
        this.menu_item = new ItemStack(material);
        consumer.accept(this.menu_item);
    }

    public Optional<ItemStack> getMenuItem() {
        return Optional.ofNullable(menu_item);
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, size,title);
        for (int s = 0;s < size;s++) inventory.setItem(s,map.containsKey(s) ? map.get(s).getIcon():NULL);
        player.openInventory(inventory);
        this.getInventories().add(inventory);
    }

    private int cast(int i) {
        if (i <= 9) return 9;
        if (54 < i) return 54;
        return (i / 9 + (i % 9 == 0 ? 0:1)) * 9;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!this.getInventories().contains(event.getClickedInventory())) return;
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        int slot = event.getSlot();
        if (!map.containsKey(slot)) return;
        Icon icon = map.get(slot);
        icon.onClick((Player) event.getWhoClicked());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        this.getInventories().remove(event.getInventory());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (this.menu_item == null) return;
            if (event.hasItem()) {
                if (event.getItem().isSimilar(this.menu_item)) {
                    this.open(event.getPlayer());
                }
            }
        }
    }
}
