package net.tagucha.jrpg;

import net.tagucha.jrpg.config.GameAreaConfig;
import net.tagucha.jrpg.event.*;
import net.tagucha.jrpg.exception.GameException;
import net.tagucha.jrpg.exception.PopulationOverException;
import net.tagucha.jrpg.item.items.Bread;
import net.tagucha.jrpg.packet.UndarkCore;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class JinroGame {
    private final PluginMain plugin;
    private final World world;

    public final GameTimer timer;
    private boolean isStarted = false;

    private final HashSet<UUID> players = new HashSet<>();
    private final HashSet<UUID> spectators = new HashSet<>();
    private final HashSet<UUID> alive = new HashSet<>();
    private final HashSet<UUID> dead = new HashSet<>();

    private final Map<GameJob,HashSet<UUID>> workers = new HashMap<>();

    private final HashSet<UUID> prayers = new HashSet<>();
    private final HashSet<UUID> talisman = new HashSet<>();
    private final Map<UUID, GameJob> jobs = new HashMap<>();
    private final Map<UUID, Integer> heart = new HashMap<>();
    private final Map<UUID, Queue<String>> message_queue = new HashMap<>();
    private final Map<UUID, Integer> logout_count = new HashMap<>();
    private final Map<UUID, BukkitRunnable> logout_schedule = new HashMap<>();
    private final GameAreaConfig area;

    public final HashSet<Skeleton> skeletons = new HashSet<>();

    public final HashSet<UUID> axe_used = new HashSet<>();
    public final HashSet<UUID> cursed = new HashSet<>();

    public JinroGame(PluginMain plugin, World world) throws GameException{
        this.plugin = plugin;
        this.world = world;
        this.area = new GameAreaConfig(plugin,this,world);
        this.timer = new GameTimer(plugin,this);
    }

    public PluginMain getPlugin() {
        return plugin;
    }

    public World getWorld() {
        return world;
    }

    public HashSet<UUID> getPlayers() {
        return players;
    }

    public HashSet<UUID> getSpectators() {
        return spectators;
    }

    public HashSet<UUID> getAlive() {
        return alive;
    }

    public HashSet<UUID> getDead() {
        return dead;
    }

    public HashSet<UUID> getWorkers(GameJob job) {
        return this.workers.getOrDefault(job,new HashSet<>());
    }

    public boolean isAlive(UUID uuid) {
        return this.getAlive().contains(uuid);
    }

    public void die(UUID uuid) {
        if (this.getAlive().contains(uuid)) {
            this.getAlive().remove(uuid);
            this.getDead().add(uuid);
            if (!this.plugin.isOnline(uuid)) {
                this.sendMessage(uuid,String.format("%s あなたは復活時間制限により死亡しました",PluginMain.getLogo(ChatColor.RED)));
            }
            this.checkGameCondition();
        }
    }

    public void checkHeart() {
        new BukkitRunnable(){
            @Override
            public void run() {
                JinroGame.this.getAlive().forEach(uuid ->
                        JinroGame.this.plugin.getPlayer(uuid).ifPresent(player -> {
                            int begin = heart.getOrDefault(player.getUniqueId(), 0);
                            player.getInventory().forEach(stack -> {
                                if (stack != null) if (stack.isSimilar(JinroGame.this.plugin.ITEMS.HEART_OF_FORTUNE)) {
                                    JinroGame.this.heart.put(uuid, JinroGame.this.heart.getOrDefault(uuid, 0) + stack.getAmount());
                                    stack.setAmount(0);
                                }
                            });
                            //占い可能回数の表示処理
                            if (heart.getOrDefault(player.getUniqueId(), 0) != begin) {
                                player.sendMessage(PluginMain.getLogo(ChatColor.RED) + " 残りの占い可能回数: " + heart.getOrDefault(player.getUniqueId(), 0) + "回");
                            }
                        }
                ));
            }
        }.runTask(this.plugin);
    }

    public void start(HashSet<UUID> players, Map<UUID,GameJob> wish, HashSet<UUID> spectators) {
        this.plugin.getGameConfig().reload();
        this.plugin.registerJinroGame(this);
        this.players.addAll(players);
        this.alive.addAll(players);
        this.spectators.addAll(spectators);
        try {
            this.giveJobs(wish);
            this.timer.start();
            for (UUID uuid:this.players) {
                this.sendMessage(uuid, String.format("%s %sあなたの役職は%s%sです",
                                PluginMain.getLogo(ChatColor.GOLD),
                                ChatColor.WHITE,
                                this.jobs.get(uuid).getDisplayName(),
                                ChatColor.WHITE
                        )
                );
                this.plugin.getPlayer(uuid).ifPresent(player -> {
                    this.message_queue.put(uuid, new ArrayDeque<>());
                    player.getInventory().clear();
                    player.getInventory().addItem();
                    player.getInventory().addItem(this.plugin.ITEMS.BLUNT, this.plugin.ITEMS.COOKED_BEEF, new ItemStack(Material.SPYGLASS));
                    player.setFoodLevel(20);
                    player.setMaxHealth(40);
                    player.setHealth(40);
                    player.setExhaustion(0);
                    player.setSaturation(40);
                    player.setGameMode(GameMode.ADVENTURE);
                    player.setFlying(false);
                    UndarkCore.addPlayer(player.getUniqueId());
                });
            }
            this.isStarted = true;
            Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
        } catch (GameException e) {
            this.plugin.noticeMessage(String.format("%s%s人数が足りないためゲームを開始できませんでした。", PluginMain.getLogo(ChatColor.GOLD), ChatColor.RED));
            this.plugin.unregisterJinroGame();
        }
    }

    public void finish(GameJob job, int code) {
        Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
        this.timer.stop();
        this.skeletons.forEach(skeleton -> {
            if (skeleton != null) skeleton.remove();
        });
        switch (code) {
            case 0:
                final String title = switch (job) {
                    case VAMPIRE -> String.format("%s♠%s%s吸血鬼の勝利%s%s♠", ChatColor.DARK_RED, ChatColor.RED, ChatColor.BOLD, ChatColor.RESET, ChatColor.DARK_RED);
                    case VILLAGER -> String.format("%s☺%s%s村人の勝利%s%s☺", ChatColor.WHITE, ChatColor.WHITE, ChatColor.BOLD, ChatColor.RESET, ChatColor.WHITE);
                    case WEREWOLF -> String.format("%s☠%s%s人狼の勝利%s%s☠", ChatColor.BLACK, ChatColor.DARK_RED, ChatColor.BOLD, ChatColor.RESET, ChatColor.BLACK);
                    default -> String.format("%s✘%s%s引き分け%s%s✘", ChatColor.GOLD, ChatColor.WHITE, ChatColor.BOLD, ChatColor.RESET, ChatColor.GOLD);
                };
                this.getPlayers().forEach(uuid -> this.plugin.getPlayer(uuid).ifPresent(player -> player.sendTitle(title, ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "GAME END")));
                this.getSpectators().forEach(uuid -> this.plugin.getPlayer(uuid).ifPresent(player -> player.sendTitle(title, ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "GAME END")));
            case 1:
                this.getPlayers().forEach(uuid -> this.plugin.getPlayer(uuid).ifPresent(player -> this.showResult(player, job)));
                this.getSpectators().forEach(uuid -> this.plugin.getPlayer(uuid).ifPresent(player -> this.showResult(player, job)));
        }
    }

    public void checkGameCondition() {
        boolean bsAlive = false, wsAlive = false, vsAlive = false;
        for (UUID uuid : this.getAlive()) {
            Optional<GameJob> optional = this.getJob(uuid);
            if (optional.isPresent()) {
                switch (optional.get().side) {
                    case 0 -> wsAlive = true;
                    case 1 -> bsAlive = true;
                    case 2 -> vsAlive = true;
                }
            }
        }
        if (bsAlive && wsAlive) return;
        else if (vsAlive) finish(GameJob.VAMPIRE,0);
        else if (bsAlive) finish(GameJob.WEREWOLF, 0);
        else if (wsAlive) finish(GameJob.VILLAGER, 0);
        else finish(null,1);
    }

    public void showResult(Player player, GameJob job) {
        List<String> args = new ArrayList<>();
        args.add(ChatColor.DARK_GREEN +"=============" + (job == null ? "=LIST=":"今回の役職") + "=============");

        Arrays.stream(GameJob.values()).filter(j -> !this.workers.get(j).isEmpty()).forEach(j -> {
            args.add(j.getRealName() + ChatColor.RESET + ":");
            args.add(
                    this.getWorkers(j).stream().map(uuid -> String.format("%s%s%s",
                            ChatColor.WHITE,
                            job == null && this.getDead().contains(uuid) ? ChatColor.GRAY : ChatColor.WHITE,
                            this.plugin.getName(uuid)
                    )).collect(Collectors.joining(ChatColor.WHITE + ",")));
        });

        args.add(ChatColor.DARK_GREEN + "================================");
        for (String str:args) player.sendMessage(str);
    }

    private int[] getNumOfJobs(int pop) {
        //W,M,L,V,B,H,F
        if (this.plugin.getGameConfig().isReloaded()) {
            if (this.plugin.getGameConfig().getJobs().containsKey(pop)) {
                return this.plugin.getGameConfig().getJobs().get(pop);
            }
        }
        return switch (pop) {
            case 3, 4, 5 -> new int[]{1, 0, 0, 0, 0, 0, 0};
            case 6, 7 -> new int[]{1, 1, 0, 0, 0, 0, 0};
            case 8, 9 -> new int[]{2, 1, 0, 0, 0, 0, 0};
            case 10, 11 -> new int[]{2, 1, 0, 1, 0, 0, 0};
            case 12, 13, 14 -> new int[]{3, 1, 0, 1, 0, 0, 0};
            case 15, 16 -> new int[]{3, 1, 0, 1, 1, 1, 0};
            default -> null;
        };
    }

    private void giveJobs(Map<UUID,GameJob> wish) throws PopulationOverException {
        List<UUID> remain = new ArrayList<>(this.players);
        Random random = new Random();
        int population = remain.size();
        int[] jobs = getNumOfJobs(population);
        if (jobs == null) throw new PopulationOverException(this,population);
        Map<GameJob,List<UUID>> wish_map = new HashMap<>();
        for (GameJob job : GameJob.values()) {
            wish_map.put(job,new ArrayList<>());
            this.workers.put(job, new HashSet<>());
        }
        for (UUID uuid:wish.keySet()) wish_map.get(wish.getOrDefault(uuid,GameJob.VILLAGER)).add(uuid);
        this.plugin.getLogger().info(Arrays.toString(jobs));
        for (GameJob job:GameJob.values()) {
            if (job == GameJob.VILLAGER) continue;
            while (jobs[job.count] > 0) if (randomGiveWishJobs(remain,random,wish_map,job)) jobs[job.count]--; else break;;
        }
        this.plugin.getLogger().info(Arrays.toString(jobs));
        for (GameJob job:GameJob.values()) {
            if (job == GameJob.VILLAGER) continue;
            for (int i = 0;i < jobs[job.count];i++) {
                UUID uuid = remain.remove(random.nextInt(remain.size()));
                this.jobs.put(uuid, job);
                this.workers.get(job).add(uuid);
            }
        }
        this.workers.put(GameJob.VILLAGER,new HashSet<>(remain));
        remain.forEach(uuid -> this.jobs.put(uuid, GameJob.VILLAGER));
        this.plugin.getLogger().info(this.workers.toString());
    }

    private boolean randomGiveWishJobs(List<UUID> remain, Random random, Map<GameJob,List<UUID>> wish_map, GameJob job) {
        if (wish_map.get(job).isEmpty()) return false;
        UUID uuid = wish_map.get(job).remove(random.nextInt(wish_map.get(job).size()));
        remain.remove(uuid);
        this.jobs.put(uuid, job);
        this.workers.get(job).add(uuid);
        return true;
    }

    public Optional<GameJob> getJob(UUID uuid) {
        return Optional.ofNullable(this.jobs.get(uuid));
    }

    public boolean pray(UUID uuid) {
        if (this.prayers.contains(uuid)) {
            return false;
        }
        if (!jobs.get(uuid).equals(GameJob.WEREWOLF)) this.prayers.add(uuid);
        return true;
    }

    public boolean useInvincible(UUID uuid) {
        if (this.jobs.get(uuid) == GameJob.VAMPIRE) return true;
        if (!this.prayers.contains(uuid)) return false;
        this.prayers.remove(uuid);
        return true;
    }

    public boolean isInvincible(UUID uuid) {
        if (this.jobs.get(uuid) == GameJob.VAMPIRE) return true;
        return this.prayers.contains(uuid);
    }

    public boolean useTalisman(UUID uuid) {
        if (this.talisman.contains(uuid)) {
            this.sendMessage(uuid, PluginMain.getLogo(ChatColor.RED) + "既に使用しています");
            return false;
        }
        this.talisman.add(uuid);
        this.sendMessage(uuid, String.format("%s %s を使用しました",PluginMain.getLogo(ChatColor.RED),"天啓の呪符"));
        return true;
    }

    public boolean hasTalisman(UUID uuid) {
        return this.talisman.contains(uuid);
    }

    public boolean fortune(UUID uuid) {
        if (this.heart.getOrDefault(uuid,0) > 0) {
            this.heart.replace(uuid,this.heart.get(uuid) - 1);
            return true;
        }
        return false;
    }

    public void spawnSkeletons() {
        for (int i = 0;i < this.plugin.getGameConfig().getSkeletonPerPopulation() * this.getPlayers().size();i++) {
            Skeleton skeleton = (Skeleton) this.world.spawnEntity(this.area.getRandom(), EntityType.SKELETON);
            skeleton.getEquipment().clear();
            skeleton.setMaxHealth(4);
            skeleton.setHealth(4);
            skeleton.setCanPickupItems(false);
            this.skeletons.add(skeleton);
        }
    }

    public void sendMessage(UUID uuid, String arg) {
        Optional<Player> player = this.plugin.getPlayer(uuid);
        if (player.isPresent()) {
            player.get().sendMessage(arg);
        } else {
            this.message_queue.get(uuid).add(arg);
        }
    }

    public void killSkeleton(Skeleton skeleton) {
        this.skeletons.remove(skeleton);
        if (!skeleton.isDead()) skeleton.remove();
    }

    public boolean isAlive(GameJob job) {
        for (UUID uuid:this.getAlive()) if (this.getJob(uuid).isPresent()) if (this.getJob(uuid).get() == job) return true;
        return false;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public static class GameListener implements Listener {
        public static final String REGISTER_YET = String.format("%s%s未登録",ChatColor.DARK_BLUE,ChatColor.BOLD);
        public static final String TO_REGISTER = String.format("%s<%s登録 %s: %s右クリック%s>",ChatColor.BLACK,ChatColor.DARK_BLUE,ChatColor.BLACK,ChatColor.DARK_BLUE,ChatColor.BLACK);
        public static final String REGISTERED = String.format("%s%s%s<NAME>",ChatColor.DARK_BLUE,ChatColor.BOLD,ChatColor.UNDERLINE);
        public static final String TO_FORTUNE = String.format("%s<%s占う %s: %s右クリック%s>",ChatColor.BLACK,ChatColor.DARK_BLUE,ChatColor.BLACK,ChatColor.DARK_BLUE,ChatColor.BLACK);

        private final PluginMain plugin;

        public GameListener(PluginMain plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onTick(GameTickEvent event) {
            this.plugin.getServer().getOnlinePlayers().stream()
                    .filter(player -> event.getGame().getDead().contains(player.getUniqueId()))
                    .filter(player -> !player.getGameMode().equals(GameMode.SPECTATOR))
                    .forEach(player -> player.setGameMode(GameMode.SPECTATOR));
            this.plugin.getServer().getOnlinePlayers().stream()
                    .filter(player -> event.getGame().getSpectators().contains(player.getUniqueId()))
                    .filter(player -> !player.getGameMode().equals(GameMode.SPECTATOR))
                    .forEach(player -> player.setGameMode(GameMode.SPECTATOR));
        }

        @EventHandler
        public void onStart(GameStartEvent event) {
            this.plugin.MERCHANT.respawn(event.getGame().getWorld());
        }

        @EventHandler
        public void onEnd(GameEndEvent event) {
            UndarkCore.getCanceler().clear();
            this.plugin.unregisterJinroGame();
        }

        @EventHandler
        public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();
            Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
            if (optional.isPresent()) {
                JinroGame game = optional.get();
                if (game.isAlive(player.getUniqueId())) {
                    game.die(player.getUniqueId());
                    game.getSpectators().forEach(uuid -> game.sendMessage(uuid, event.getDeathMessage()));
                    game.getDead().forEach(uuid ->  game.sendMessage(uuid,event.getDeathMessage()));
                    event.setDeathMessage(null);
                    event.getDrops().clear();
                    UndarkCore.removePlayer(player.getUniqueId());
                }
            }
        }

        @EventHandler
        public void onRespawn(PlayerRespawnEvent event) {
            Player player = event.getPlayer();
            Optional<JinroGame> opt = this.plugin.isPlayer(player.getUniqueId());
            if (opt.isPresent()) {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        player.setGameMode(GameMode.SPECTATOR);
                    }
                }.runTask(this.plugin);
            }
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            this.plugin.isPlayer(player.getUniqueId()).ifPresent(game -> {
                player.sendMessage(PluginMain.getLogo(ChatColor.AQUA) + " あなたの役職は" + game.getJob(player.getUniqueId()).get().getDisplayName());
                if (game.message_queue.get(player.getUniqueId()).isEmpty()) {
                    player.sendMessage(PluginMain.getLogo(ChatColor.AQUA) + " ログアウト中に届いたメッセージはありません。");
                } else {
                    player.sendMessage(PluginMain.getLogo(ChatColor.AQUA) + " ログアウト中に以下のメッセージが届きました");
                    while (!game.message_queue.get(player.getUniqueId()).isEmpty()) player.sendMessage(game.message_queue.get(player.getUniqueId()).remove());
                    player.sendMessage(PluginMain.getLogo(ChatColor.AQUA) + " 以上です。");
                }
                game.logout_schedule.remove(player.getUniqueId()).cancel();
            });
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            this.plugin.isPlayer(uuid).ifPresent(game -> {
                event.setQuitMessage(String.format("%s %sがゲームから退出しました。一定時間が経過すると死亡判定がくだされます。",PluginMain.getLogo(ChatColor.RED),event.getPlayer().getName()));
                game.logout_count.put(uuid, game.logout_count.getOrDefault(uuid,0) + 1);
                int count = game.logout_count.get(uuid);
                plugin.getLogger().info(PluginMain.getLogo(ChatColor.RED) + " ログアウトカウント: " + count);
                long limit = (long) (120 - Math.pow(count - 1,3));
                if (limit <= 0) game.die(uuid);
                else {
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            game.die(uuid);
                            game.logout_schedule.remove(uuid);
                            for (UUID uuid:game.getAlive()) game.sendMessage(uuid, PluginMain.getLogo(ChatColor.GOLD) + " " + event.getPlayer().getName() + "はログアウト時間超過のため死亡しました");
                            for (UUID uuid:game.getDead()) plugin.getPlayer(uuid).ifPresent(player -> player.sendMessage(PluginMain.getLogo(ChatColor.GOLD) + " " + event.getPlayer().getName() + "はログアウト時間超過のため死亡しました"));
                            for (UUID uuid:game.getSpectators()) plugin.getPlayer(uuid).ifPresent(player -> player.sendMessage(PluginMain.getLogo(ChatColor.GOLD) + " " + event.getPlayer().getName() + "はログアウト時間超過のため死亡しました"));
                        }
                    };
                    runnable.runTaskLater(this.plugin, limit * 20);
                    game.logout_schedule.put(uuid, runnable);
                }
            });
        }

        @EventHandler
        public void onSkeletonDeath(EntityDeathEvent event) {
            if (event.getEntityType() == EntityType.SKELETON) this.plugin.getJinroGame().ifPresent(game -> {
                game.killSkeleton((Skeleton) event.getEntity());
                event.setDroppedExp(0);
                event.getDrops().clear();
                if (new Random().nextDouble() < this.plugin.getGameConfig().getEmeraldOdds()) {
                    Player player = event.getEntity().getKiller();
                    if (player == null) return;
                    player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
                }
            });
        }

        @EventHandler
        public void onChangeToDay(ChangeToDayEvent event) {
            event.getGame().skeletons.forEach(skeleton -> {
                if (skeleton != null) skeleton.remove();
            });
            event.getGame().prayers.clear();
            event.getGame().talisman.clear();
            if (event.getGame().workers.get(GameJob.BAKER).stream().anyMatch(event.getGame().alive::contains))
                event.getGame().getAlive().forEach(uuid -> this.plugin.getPlayer(uuid).ifPresent(player -> player.getInventory().addItem(new Bread(this.plugin).getItemStack(1, event.getGame().timer.getDay()))));
            event.getGame().axe_used.clear();
            for (UUID uuid:event.getGame().getAlive()) event.getGame().sendMessage(uuid, PluginMain.getLogo(ChatColor.GOLD) + " 昼になりました");
            for (UUID uuid:event.getGame().getDead()) this.plugin.getPlayer(uuid).ifPresent(player -> player.sendMessage(PluginMain.getLogo(ChatColor.GOLD) + " 昼になりました"));
            for (UUID uuid:event.getGame().getSpectators()) this.plugin.getPlayer(uuid).ifPresent(player -> player.sendMessage(PluginMain.getLogo(ChatColor.GOLD) + " 昼になりました"));
        }

        @EventHandler
        public void onChangeToNight(ChangeToNightEvent event) {
            event.getGame().spawnSkeletons();
            for (UUID uuid:event.getGame().getAlive()) event.getGame().sendMessage(uuid, PluginMain.getLogo(ChatColor.GOLD) + " 夜になりました");
            for (UUID uuid:event.getGame().getDead()) this.plugin.getPlayer(uuid).ifPresent(player -> player.sendMessage(PluginMain.getLogo(ChatColor.GOLD) + " 夜になりました"));
            for (UUID uuid:event.getGame().getSpectators()) this.plugin.getPlayer(uuid).ifPresent(player -> player.sendMessage(PluginMain.getLogo(ChatColor.GOLD) + " 夜になりました"));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onClickingSign(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            Block block = event.getClickedBlock();
            if (isSign(block.getType())) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(1).equalsIgnoreCase(REGISTER_YET) && sign.getLine(3).equalsIgnoreCase(TO_REGISTER)) {
                    for (int x = sign.getX() - 12; x <= sign.getX() + 12; x++)
                        for (int y = sign.getY() - 12; y <= sign.getY() + 12; y++)
                            for (int z = sign.getZ() - 12; z <= sign.getZ() + 12; z++) {
                                Block b = new Location(player.getWorld(), x, y, z).getBlock();
                                if (isSign(b.getType())) {
                                    Sign s = (Sign) b.getState();
                                    if (!(s.getLine(1).equalsIgnoreCase(REGISTERED.replace("<NAME>", event.getPlayer().getName())) && s.getLine(3).equalsIgnoreCase(TO_FORTUNE)))
                                        continue;
                                    event.getPlayer().sendMessage(PluginMain.getLogo(ChatColor.RED) + " 既に他の看板に登録しています");
                                    return;
                                }
                            }
                    sign.setLine(1, REGISTERED.replace("<NAME>", event.getPlayer().getName()));
                    sign.setLine(3, TO_FORTUNE);
                    sign.update();
                    event.getPlayer().sendMessage(PluginMain.getLogo(ChatColor.RED) + " 看板を登録しました");
                    return;
                }
                Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
                if (optional.isPresent() && !player.getInventory().getItemInMainHand().isSimilar(this.plugin.ITEMS.PROVIDENCE_OF_KNIGHT)) {
                    JinroGame game = optional.get();
                    for (UUID uuid : game.getPlayers()) {
                        if (!(sign.getLine(1).equalsIgnoreCase("§1§l§n" + this.plugin.getName(uuid)) && sign.getLine(3).equalsIgnoreCase(TO_FORTUNE))) continue;
                        if (event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                            if (game.getAlive().contains(event.getPlayer().getUniqueId())) if (game.timer.isDay()) {
                                event.getPlayer().sendMessage(PluginMain.getLogo(ChatColor.RED) + " 占いは夜のみ可能です");
                                return;
                            }
                            if (!game.fortune(event.getPlayer().getUniqueId())) {
                                event.getPlayer().sendMessage(PluginMain.getLogo(ChatColor.RED) + " 占い可能回数が0です");
                                return;
                            }
                        }
                        String arg = game.getJob(uuid).get().getFortuneResult();
                        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                            if (game.getJob(uuid).isPresent()) if (game.getJob(uuid).get() == GameJob.MAD)
                                arg += ChatColor.GRAY + "(" + GameJob.MAD.getRealName() + ")";
                            if (game.getDead().contains(uuid)) arg += ChatColor.GRAY + "(死亡)";
                        }
                        event.getPlayer().sendMessage(PluginMain.getLogo(ChatColor.RED) + " 占い結果 : " + arg);
                        if (game.isAlive(player.getUniqueId())) {
                            player.sendMessage(PluginMain.getLogo(ChatColor.RED) + " 残りの占い可能回数: " + game.heart.getOrDefault(player.getUniqueId(), 0) + "回");
                            Bukkit.getPluginManager().callEvent(new GameFortuneEvent(game, event.getPlayer(), uuid));
                        }
                        return;
                    }
                }
            }
        }

        @EventHandler
        public void onFortune(GameFortuneEvent event) {
            if (event.getGame().hasTalisman(event.getTarget())) event.getGame().sendMessage(event.getTarget(),String.format("%s[天啓の呪符] %sあなたは占われました",ChatColor.GRAY,ChatColor.RED));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onDamage(EntityDamageEvent event) {
            if (event.getEntity() instanceof Player player) {
                UUID uuid = player.getUniqueId();
                this.plugin.isPlayer(uuid).ifPresent(game -> {
                    if (game.isInvincible(uuid)) {
                        if (player.getHealth() <= event.getDamage()) {
                            game.useInvincible(uuid);
                            event.setDamage(0);
                            player.setHealth(40);
                        }
                    }
                });
            }
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onChat(AsyncPlayerChatEvent event) {
            this.plugin.getJinroGame().ifPresent(game -> {
                event.setCancelled(true);
                if (game.getAlive().contains(event.getPlayer().getUniqueId())) {
                    if (game.timer.isNight()) {
                        event.getPlayer().sendMessage(String.format("%s 夜間にチャットは使えません",PluginMain.getLogo(ChatColor.RED)));
                        return;
                    }
                    String message = String.format("%s %s: %s",PluginMain.getLogo(ChatColor.DARK_AQUA),event.getPlayer().getName(),event.getMessage());
                    this.plugin.getOnlinePlayers().forEach(player -> player.sendMessage(message));
                } else {
                    String message = String.format("%s %s: %s",PluginMain.getLogo(ChatColor.GRAY),event.getPlayer().getName(),event.getMessage());
                    this.plugin.getOnlinePlayers().forEach(player -> {
                        if (!game.getAlive().contains(player.getUniqueId())) game.sendMessage(player.getUniqueId(), message);
                    });
                }
            });
        }

        public static boolean isSign(Material material) {
            return switch (material) {
                case SPRUCE_SIGN, SPRUCE_WALL_SIGN, ACACIA_SIGN, ACACIA_WALL_SIGN, BIRCH_SIGN, BIRCH_WALL_SIGN, DARK_OAK_SIGN, DARK_OAK_WALL_SIGN, JUNGLE_SIGN, JUNGLE_WALL_SIGN, OAK_SIGN, OAK_WALL_SIGN, CRIMSON_SIGN, CRIMSON_WALL_SIGN, WARPED_SIGN, WARPED_WALL_SIGN -> true;
                default -> false;
            };
        }

        @EventHandler
        public void onDisable(PluginDisableEvent event) {
            this.plugin.getJinroGame().ifPresent(game -> game.finish(null, 1));
        }
    }
}
