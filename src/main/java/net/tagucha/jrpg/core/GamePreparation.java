package net.tagucha.jrpg.core;

import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.job.GameJob;
import net.tagucha.jrpg.menu.Icon;
import net.tagucha.jrpg.menu.Menu;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import net.tagucha.jrpg.exception.GameException;

import java.util.*;

public class GamePreparation implements Listener {
    private static final String TITLE = String.format("%s%s開始まで%s秒", ChatColor.GOLD, ChatColor.BOLD, "%d");
    private static final ClickEvent CLICK_EVENT = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jinro join");
    private static final HoverEvent COMMAND_MESSAGE = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(ChatColor.WHITE + "メッセージをクリックで参加できます。")});

    private final PluginMain plugin;
    private final int max_time;
    private int time;
    private final Map<UUID, GameJob> wish = new HashMap<>();
    private final HashSet<UUID> players = new HashSet<>();
    private final HashSet<UUID> spectator = new HashSet<>();
    private final BossBar bar;
    private final JinroGame game;
    private TimerThread nextThread = null;

    private final Menu dream_menu;

    public GamePreparation(PluginMain plugin, World world, int time) throws GameException {
        this.plugin = plugin;
        this.max_time = time;
        this.time = time;
        this.bar = Bukkit.createBossBar(String.format(TITLE, time), BarColor.WHITE, BarStyle.SEGMENTED_10);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.game = new JinroGame(this.plugin, world);

        this.dream_menu = new Menu(plugin,String.format("%s%s希望する役職を選んでください", ChatColor.GOLD, ChatColor.BOLD),27);
        this.dream_menu.setMenuItem(Material.CLOCK, stack -> {
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(String.format("%s役職希望メニュー",PluginMain.getLogo(ChatColor.GOLD)));
            meta.setLore(Collections.singletonList(String.format("%s右クリックで使用できます",ChatColor.WHITE)));
            stack.setItemMeta(meta);
        });

        this.dream_menu.add(0, Icon.createDummyIcon(Material.WHEAT, 1));
        this.dream_menu.add(1, Icon.createDummyIcon(Material.BLACK_STAINED_GLASS_PANE, 1));
        this.dream_menu.add(2, new Icon(Material.WHEAT, GameJob.VILLAGER.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.VILLAGER);
                player.closeInventory();
            }
        });
        this.dream_menu.add(3, new Icon(Material.POISONOUS_POTATO, GameJob.LYCANTHROPY.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.LYCANTHROPY);
                player.closeInventory();
            }
        });
        this.dream_menu.add(4, new Icon(Material.BREAD, GameJob.BAKER.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.BAKER);
                player.closeInventory();
            }
        });
        this.dream_menu.add(5, new Icon(Material.BOW, GameJob.HUNTER.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.HUNTER);
                player.closeInventory();
            }
        });
        this.dream_menu.add(6, new Icon(Material.WRITABLE_BOOK, GameJob.CORONER.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.CORONER);
                player.closeInventory();
            }
        });

        this.dream_menu.add(9, Icon.createDummyIcon(Material.STONE_AXE, 1));
        this.dream_menu.add(10, Icon.createDummyIcon(Material.BLACK_STAINED_GLASS_PANE, 1));
        this.dream_menu.add(11, new Icon(Material.STONE_AXE, GameJob.WEREWOLF.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.WEREWOLF);
                player.closeInventory();
            }
        });
        this.dream_menu.add(12, new Icon(Material.ENDER_EYE, GameJob.MAD.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.MAD);
                player.closeInventory();
            }
        });
        this.dream_menu.add(13, new Icon(Material.WRITTEN_BOOK, GameJob.SMART_WEREWOLF.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.SMART_WEREWOLF);
                player.closeInventory();
            }
        });
        this.dream_menu.add(14, new Icon(Material.BEEF, GameJob.INSANE_WEREWOLF.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.INSANE_WEREWOLF);
                player.closeInventory();
            }
        });

        this.dream_menu.add(18, Icon.createDummyIcon(Material.NETHER_STAR,1));
        this.dream_menu.add(19, Icon.createDummyIcon(Material.BLACK_STAINED_GLASS_PANE, 1));
        this.dream_menu.add(20, new Icon(Material.NETHER_STAR, GameJob.VAMPIRE.getRealName(), new ArrayList<>()) {
            @Override
            public void onClick(Player player) {
                GamePreparation.this.wish(player, GameJob.VAMPIRE);
                player.closeInventory();
            }
        });
    }

    public void start() {
        this.nextThread = new TimerThread();
        this.nextThread.runTaskLater(GamePreparation.this.plugin, 20);
        this.plugin.getLogger().info("JinroRPGの募集が開始されました.");
        this.plugin.noticeMessage(String.format("%s ゲームの募集を開始しました",
                PluginMain.getLogo(ChatColor.GOLD)
        ), CLICK_EVENT, COMMAND_MESSAGE);
    }

    public boolean next() {
        time--;
        if (time <= 0) {
            this.bar.removeAll();
            this.game.start(players, wish, spectator);
            return false;
        }
        for (Player player:Bukkit.getOnlinePlayers()) this.bar.addPlayer(player);
        this.bar.setProgress(((double) time) / ((double) max_time));
        this.bar.setTitle(String.format(TITLE, time));
        return true;
    }

    public void sendAddMessage(Player player) {
        this.plugin.noticeMessage(String.format("%s %sさんがゲームに参加しました",
                PluginMain.getLogo(ChatColor.GOLD),
                player.getName()
        ), CLICK_EVENT, COMMAND_MESSAGE);
    }

    public void addPlayer(Player player) {
        if (this.players.contains(player.getUniqueId())) {
            player.sendMessage(String.format("%s すでに参加しています",PluginMain.getLogo(ChatColor.GOLD)));
            return;
        }
        this.players.add(player.getUniqueId());
        this.sendAddMessage(player);
        player.sendMessage(String.format("%s 希望する役職があれば時計をクリックしてください",PluginMain.getLogo(ChatColor.GOLD)));
        this.dream_menu.getMenuItem().ifPresent(item -> player.getInventory().addItem(item));
    }

    public void addSpectator(Player player) {
        this.spectator.add(player.getUniqueId());
        player.sendMessage(String.format("%s %sスペックテイターになりました", PluginMain.getLogo(ChatColor.GRAY), ChatColor.GRAY));
    }

    private class TimerThread extends BukkitRunnable {
        @Override
        public void run() {
            if (!GamePreparation.this.next()) return;
            GamePreparation.this.nextThread = new TimerThread();
            GamePreparation.this.nextThread.runTaskLater(GamePreparation.this.plugin, 20);
        }
    }

    public void wish(Player player, GameJob job) {
        this.wish.put(player.getUniqueId(), job);
        player.sendMessage(PluginMain.getLogo(ChatColor.AQUA) + " " + job.getRealName() + ChatColor.WHITE + "を希望しました");
    }

    public void cancelGame() {
        Optional.ofNullable(this.nextThread).ifPresent(BukkitRunnable::cancel);
        if (this.game.isStarted()) this.game.finish(null, 1);
        else {
            this.bar.removeAll();
        }
    }
}
