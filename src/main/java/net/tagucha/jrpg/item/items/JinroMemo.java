package net.tagucha.jrpg.item.items;

import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.event.GameFortuneEvent;
import net.tagucha.jrpg.event.GameStartEvent;
import net.tagucha.jrpg.event.GameUseEyeOfMadEvent;
import net.tagucha.jrpg.item.GameItem;
import net.tagucha.jrpg.item.ItemPermission;
import net.tagucha.jrpg.item.TimePermission;
import net.tagucha.jrpg.job.GameJob;
import net.tagucha.jrpg.menu.Icon;
import net.tagucha.jrpg.menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JinroMemo extends GameItem {
    private final TreeSet<COSet> coSets = new TreeSet<>();
    private final Map<UUID, Set<COSet>> accuserToSet = new HashMap<>();
    private final Map<UUID, Set<COSet>> targetToSet = new HashMap<>();
    private final Map<UUID, Set<UUID>> fortunes = new HashMap<>();

    public JinroMemo(JinroRPG plugin) {
        super(
                plugin,
                Material.WRITTEN_BOOK,
                String.format("%s人狼メモ", ChatColor.GOLD),
                Collections.singletonList(String.format("%s占い結果やプレイヤーの確認などができる", ChatColor.WHITE)),
                128,
                ItemPermission.ANYONE,
                TimePermission.ANYTIME,
                new ConfigKey("jinro_memo", ItemType.OTHER)
        );
    }

    private void addCOSet(UUID accuser, UUID target, String result) {
        this.plugin.getJinroGame().ifPresent(game -> {
            if (game.timer.isNight()) {
                game.sendMessage(accuser, JinroRPG.getChatLogo(ChatColor.RED) + " COは昼間にしかできません。");
                return;
            }
            COSet set = new COSet(game.timer.getTotalTime(), accuser, target, result);
            if (!accuserToSet.containsKey(accuser)) this.accuserToSet.put(accuser, new HashSet<>());
            if (!targetToSet.containsKey(accuser)) this.targetToSet.put(accuser, new HashSet<>());
            accuserToSet.get(accuser).add(set);
            targetToSet.get(accuser).add(set);
            coSets.add(set);
            this.plugin.noticeMessage(JinroRPG.getChatLogo(ChatColor.AQUA) + " CO: " + set.toFullLore(plugin));
        });
    }

    private void addCOSet(UUID accuser, UUID target, GameJob job) {
        this.addCOSet(accuser, target, job.getRealName());
    }

    @EventHandler
    public void onStart(GameStartEvent event) {
        this.coSets.clear();
        this.accuserToSet.clear();
        this.targetToSet.clear();
        this.fortunes.clear();
    }

    @EventHandler
    public void onFortune(GameFortuneEvent event) {
        if (!fortunes.containsKey(event.getForerunner().getUniqueId())) fortunes.put(event.getForerunner().getUniqueId(), new HashSet<>());
        fortunes.get(event.getForerunner().getUniqueId()).add(event.getTarget());
    }

    @EventHandler
    public void onUseEyeOfMad(GameUseEyeOfMadEvent event) {
        if (!fortunes.containsKey(event.getMad().getUniqueId())) fortunes.put(event.getMad().getUniqueId(), new HashSet<>());
        fortunes.get(event.getMad().getUniqueId()).add(event.getTarget());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) return;
        if (this.plugin.ITEMS.getItem(this).isSimilar(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            Menu menu = new Menu(this.plugin, String.format("%s%s人狼メモ", ChatColor.BLACK, ChatColor.BOLD), 27);
            menu.add(10, new Icon(Material.PLAYER_HEAD, String.format("%sプレイヤーリスト", ChatColor.GOLD), Collections.singletonList(String.format("%s人狼RPGのプレイヤーリストを閲覧できる", ChatColor.WHITE))) {
                @Override
                public void onClick(Player player) {
                    plugin.getJinroGame().ifPresent(game -> {
                        Menu player_list = new Menu(plugin, String.format("%s%s人狼メモ", ChatColor.BLACK, ChatColor.BOLD), game.getPlayers().size());
                        List<UUID> list = new ArrayList<>(game.getPlayers());
                        for (int i = 0;i < Math.min(list.size(), 54);i++) {
                            final UUID target = list.get(i);
                            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta meta = (SkullMeta) head.getItemMeta();
                            plugin.getProfile(list.get(i)).ifPresent(profile -> {
                                meta.setOwnerProfile(profile);
                                meta.setDisplayName(String.format("%s%s%s", ChatColor.WHITE, ChatColor.BOLD, profile.getName()));
                            });
                            head.setItemMeta(meta);
                            player_list.add(i, new Icon(head) {
                                @Override
                                public void onClick(Player player) {
                                    String title = player.getUniqueId().equals(target) ? String.format("%s%s自分の役職をCOできます", ChatColor.BLACK, ChatColor.BOLD) : String.format("%s%s%sの役職をCOできます", ChatColor.BLACK, ChatColor.BOLD, plugin.getName(target));
                                    Menu fco = new Menu(plugin, title, 27);
                                    fco.add(0, new Icon(Material.WHEAT, ChatColor.GREEN.toString() + ChatColor.BOLD + "村人陣営", new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, ChatColor.GREEN.toString() + ChatColor.BOLD + "村人陣営");
                                        }
                                    });
                                    fco.add(1, Icon.createDummyIcon(Material.BLACK_STAINED_GLASS_PANE, 1));
                                    fco.add(2, new Icon(Material.WHEAT, GameJob.VILLAGER.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.VILLAGER);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.add(3, new Icon(Material.POISONOUS_POTATO, GameJob.LYCANTHROPY.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.LYCANTHROPY);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.add(4, new Icon(Material.BREAD, GameJob.BAKER.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.BAKER);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.add(5, new Icon(Material.BOW, GameJob.HUNTER.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.HUNTER);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.add(6, new Icon(Material.WRITABLE_BOOK, GameJob.CORONER.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.CORONER);
                                            player.closeInventory();
                                        }
                                    });

                                    fco.add(9, new Icon(Material.STONE_AXE, ChatColor.DARK_RED.toString() + ChatColor.BOLD + "人狼陣営", new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, ChatColor.DARK_RED.toString() + ChatColor.BOLD + "人狼陣営");
                                        }
                                    });
                                    fco.add(10, Icon.createDummyIcon(Material.BLACK_STAINED_GLASS_PANE, 1));
                                    fco.add(11, new Icon(Material.STONE_AXE, GameJob.WEREWOLF.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.WEREWOLF);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.add(12, new Icon(Material.ENDER_EYE, GameJob.MAD.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.MAD);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.add(13, new Icon(Material.WRITTEN_BOOK, GameJob.SMART_WEREWOLF.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.SMART_WEREWOLF);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.add(14, new Icon(Material.BEEF, GameJob.INSANE_WEREWOLF.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.INSANE_WEREWOLF);
                                            player.closeInventory();
                                        }
                                    });

                                    fco.add(18, new Icon(Material.NETHER_STAR, ChatColor.RED.toString() + ChatColor.BOLD + "第３陣営", new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, ChatColor.RED.toString() + ChatColor.BOLD + "第３陣営");
                                        }
                                    });
                                    fco.add(19, Icon.createDummyIcon(Material.BLACK_STAINED_GLASS_PANE, 1));
                                    fco.add(20, new Icon(Material.NETHER_STAR, GameJob.VAMPIRE.getRealName(), new ArrayList<>()) {
                                        @Override
                                        public void onClick(Player player) {
                                            addCOSet(player.getUniqueId(), target, GameJob.VAMPIRE);
                                            player.closeInventory();
                                        }
                                    });
                                    fco.open(player);
                                }
                            });
                        }
                        player_list.open(player);
                    });
                }
            });
            menu.add(12, new Icon(Material.PAPER, ChatColor.GOLD + "COメモ", Collections.singletonList(ChatColor.WHITE + "COの記録を閲覧できる")) {
                @Override
                public void onClick(Player player) {
                    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta meta = (BookMeta) book.getItemMeta();
                    meta.setAuthor("JinroRPG");
                    meta.setGeneration(BookMeta.Generation.ORIGINAL);
                    meta.setTitle("COメモ");
                    int day = 1;
                    List<String> list = new ArrayList<>();
                    list.add("[1日目]");
                    for (COSet coSet : coSets) {
                        while (coSet.time > (long) (plugin.getGameConfig().getTimeDay() + plugin.getGameConfig().getTimeNight()) * (day - 1) + plugin.getGameConfig().getTimeFirstDay()) {
                            if (list.size() > 1) meta.addPage(String.join("\n", list));
                            day++;
                            list.clear();
                            list.add(String.format("[%d日目]", day));
                        }
                        if (list.size() == 7) {
                            meta.addPage(String.join("\n", list));
                            list.clear();
                            list.add(String.format("[%d日目]", day));
                        }
                        list.add(coSet.toFullLore(plugin));
                    }
                    if (list.size() > 1) meta.addPage(String.join("\n", list));
                    if (!meta.hasPages()) meta.addPage("まだCOの記録はありません");
                    book.setItemMeta(meta);
                    player.openBook(book);
                }
            });
            menu.add(14, new Icon(Material.HEART_OF_THE_SEA, ChatColor.GOLD + "占いメモ", Collections.singletonList(ChatColor.WHITE + "自分の占いや共犯者の目の記録を閲覧できる")) {
                @Override
                public void onClick(Player player) {
                    Menu fortune = new Menu(plugin, ChatColor.BLACK + "占い結果", fortunes.size());
                    plugin.getJinroGame().ifPresent(game -> {
                        List<UUID> list = new ArrayList<>(fortunes.getOrDefault(player.getUniqueId(), new HashSet<>()));
                        for (int i = 0;i < Math.min(list.size(), 54);i++) {
                            final UUID target = list.get(i);
                            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta meta = (SkullMeta) head.getItemMeta();
                            plugin.getProfile(list.get(i)).ifPresent(profile -> {
                                meta.setOwnerProfile(profile);
                                meta.setDisplayName(String.format("%s%s%s %s", ChatColor.WHITE, ChatColor.BOLD, profile.getName(), game.getJob(target).map(GameJob::getFortuneResult).orElse(ChatColor.RED + "ERROR")));
                            });
                            head.setItemMeta(meta);
                            fortune.add(i, new Icon(head) {
                                @Override
                                public void onClick(Player player) {
                                }
                            });
                        }
                    });
                    fortune.open(player);
                }
            });
            menu.add(16, new Icon(Material.STONE_AXE, ChatColor.GOLD + "仲間メモ", Collections.singletonList(ChatColor.WHITE + "人狼が仲間を確認できる")) {
                @Override
                public void onClick(Player player) {
                    plugin.isPlayer(player.getUniqueId()).ifPresent(game -> {
                        if (game.getJob(player.getUniqueId()).filter(job -> job.side == 1).isEmpty()) return;
                        Set<UUID> werewolf_side = new HashSet<>();
                        for (UUID uuid:game.getPlayers()) {
                            if (game.getJob(uuid).filter(gameJob -> gameJob.side == 1).isPresent()) werewolf_side.add(uuid);
                        }
                        List<UUID> list = werewolf_side.stream().toList();
                        Menu partners = new Menu(plugin, ChatColor.BLACK + "仲間メモ", list.size());
                        for (int i = 0;i < Math.min(list.size(), 54);i++) {
                            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta meta = (SkullMeta) head.getItemMeta();
                            plugin.getProfile(list.get(i)).ifPresent(profile -> {
                                meta.setOwnerProfile(profile);
                                meta.setDisplayName(String.format("%s%s%s", ChatColor.WHITE, ChatColor.BOLD, profile.getName()));
                            });
                            head.setItemMeta(meta);
                            partners.add(i, new Icon(head) {
                                @Override
                                public void onClick(Player player) {
                                }
                            });
                        }
                        partners.open(player);
                    });
                }
            });
            menu.open(event.getPlayer());
        }
    }

    public static class COSet implements Comparable<COSet> {
        public final long time;
        public final UUID accuser;
        public final UUID target;
        public final String result;

        public COSet(long time, UUID accuser, UUID target, String result) {
            this.time = time;
            this.accuser = accuser;
            this.target = target;
            this.result = result;
        }

        public String toFullLore(JinroRPG plugin) {
            return String.format("%s%s%s%s => %s%s %s", ChatColor.RESET, ChatColor.BOLD, plugin.getName(this.accuser), ChatColor.RESET, ChatColor.BOLD, plugin.getName(this.target), result);
        }

        @Override
        public int compareTo(@NotNull JinroMemo.COSet o) {
            int c = Long.compare(this.time, o.time);
            return c == 0 ? this.accuser.compareTo(o.accuser) : c;
        }
    }
}
