package net.tagucha.jrpg.core;

import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.config.GameAreaConfig;
import net.tagucha.jrpg.event.GameEndEvent;
import net.tagucha.jrpg.event.GameStartEvent;
import net.tagucha.jrpg.exception.GameException;
import net.tagucha.jrpg.exception.PopulationOverException;
import net.tagucha.jrpg.job.GameJob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

public class JinroGame {
    private final JinroRPG plugin;
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

    private final HashSet<UUID> axe_used = new HashSet<>();
    private final HashSet<UUID> cursed = new HashSet<>();

    public JinroGame(JinroRPG plugin, World world) throws GameException{
        this.plugin = plugin;
        this.world = world;
        this.area = new GameAreaConfig(plugin,this,world);
        this.timer = new GameTimer(plugin,this);
    }

    public JinroRPG getPlugin() {
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

    public boolean isDead(UUID uuid) {
        return this.getDead().contains(uuid);
    }

    public boolean isAlive(GameJob job) {
        for (UUID uuid:this.getAlive()) if (this.getJob(uuid).isPresent()) if (this.getJob(uuid).get() == job) return true;
        return false;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public Map<UUID, Queue<String>> getMessageQueue() {
        return message_queue;
    }

    public Map<UUID, Integer> getLogoutCount() {
        return logout_count;
    }

    public Map<UUID, BukkitRunnable> getLogoutSchedule() {
        return logout_schedule;
    }

    public HashSet<UUID> getPrayers() {
        return prayers;
    }

    public HashSet<UUID> getTalisman() {
        return talisman;
    }

    public Map<UUID, Integer> getHeart() {
        return heart;
    }

    public HashSet<UUID> getAxeUsed() {
        return axe_used;
    }

    public HashSet<UUID> getCursed() {
        return cursed;
    }

    public GameAreaConfig getArea() {
        return area;
    }

    public void start(HashSet<UUID> players, Map<UUID,GameJob> wish, HashSet<UUID> spectators) {
        this.plugin.getGameConfig().reload();
        this.plugin.registerJinroGame(this);
        this.players.addAll(players);
        this.alive.addAll(players);
        this.spectators.addAll(spectators);
        Team temp = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("jinro");
        if (temp == null) temp = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("jinro");
        final Team team = temp;
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        try {
            this.giveJobs(wish);
            this.timer.start();
            this.noticeMessage(String.format("%s 役職配分:", JinroRPG.getChatLogo(ChatColor.GOLD)));
            this.noticeMessage(Arrays.stream(GameJob.values())
                    .filter(job -> this.workers.get(job).size() > 0)
                    .map(job -> String.format("%s%s: %d人", job.getDisplayName(), ChatColor.WHITE, this.workers.get(job).size()))
                    .collect(Collectors.joining(", ")));
            for (UUID uuid:this.players) {
                this.sendMessage(uuid, String.format("%s %sあなたの役職は%s%sです",
                                JinroRPG.getChatLogo(ChatColor.GOLD),
                                ChatColor.WHITE,
                                this.jobs.get(uuid).getDisplayName(),
                                ChatColor.WHITE
                        )
                );
                this.plugin.getPlayer(uuid).ifPresent(player -> {
                    this.message_queue.put(uuid, new ArrayDeque<>());
                    team.addPlayer(player);
                    player.getInventory().clear();
                    player.getInventory().addItem();
                    player.getInventory().addItem(this.plugin.ITEMS.BLUNT, this.plugin.ITEMS.COOKED_BEEF, this.plugin.ITEMS.TELESCOPE, this.plugin.ITEMS.MEMO);
                    player.setFoodLevel(20);
                    player.setMaxHealth(40);
                    player.setHealth(40);
                    player.setExhaustion(0);
                    player.setSaturation(40);
                    player.setGameMode(GameMode.ADVENTURE);
                    player.setAllowFlight(false);
                    this.getArea().getSpawnPoint().ifPresent(player::teleport);
                    Arrays.stream(PotionEffectType.values()).filter(player::hasPotionEffect).forEach(player::removePotionEffect);
                    for (Player other:players.stream().map(plugin::getPlayer).filter(Optional::isPresent).map(Optional::get).toList()) {
                        if (player.equals(other)) continue;
//                        ((CraftPlayer) player).getHandle().b.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, ((CraftPlayer)other).getHandle()));
                        player.setPlayerListName(" ");
                    }

                });
            }
            for (UUID uuid:this.spectators) {
                this.plugin.getPlayer(uuid).ifPresent(player -> {
                    player.setGameMode(GameMode.SPECTATOR);
                });
            }
            this.isStarted = true;
            Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
        } catch (GameException e) {
            this.plugin.noticeMessage(String.format("%s %s人数が足りないためゲームを開始できませんでした。", JinroRPG.getChatLogo(ChatColor.GOLD), ChatColor.RED));
            Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
            this.plugin.unregisterJinroGame();
        }
    }

    public void finish(GameJob job, int code) {
        Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
        try {
            Optional.ofNullable(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("jinro")).ifPresent(Team::unregister);
        } catch (NullPointerException ignored) {
        }
        this.timer.stop();
        this.skeletons.forEach(skeleton -> {
            if (skeleton != null) skeleton.remove();
        });
        this.players.stream().map(plugin::getPlayer).filter(Optional::isPresent).map(Optional::get).forEach(player -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
        });
        this.spectators.stream().map(plugin::getPlayer).filter(Optional::isPresent).map(Optional::get).forEach(player -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
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

    public void die(UUID uuid) {
        if (this.getAlive().contains(uuid)) {
            this.getAlive().remove(uuid);
            this.getDead().add(uuid);
            if (!this.plugin.isOnline(uuid)) {
                this.sendMessage(uuid,String.format("%s あなたは復活時間制限により死亡しました", JinroRPG.getChatLogo(ChatColor.RED)));
            }
            this.checkGameCondition();
        }
    }

    public void addSpectator(UUID uuid) {
        this.spectators.add(uuid);
        this.plugin.getPlayer(uuid).ifPresent(player -> player.setGameMode(GameMode.SPECTATOR));
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
        if (vsAlive) finish(GameJob.VAMPIRE,0);
        else if (bsAlive) finish(GameJob.WEREWOLF, 0);
        else if (wsAlive) finish(GameJob.VILLAGER, 0);
        else finish(null,1);
    }

    public void showResult(Player player, GameJob job) {
        List<String> args = new ArrayList<>();
        args.add(ChatColor.DARK_GREEN +"=============" + (job == null ? "=LIST=":"今回の役職") + "=============");
        if (job != null) {
            args.add("勝者: " + job.getRealName());
            args.add(" ");
        }
        Arrays.stream(GameJob.values()).filter(j -> !this.workers.get(j).isEmpty()).forEach(j -> {
            args.add(j.getRealName() + ChatColor.RESET + ":");
            args.add(
                    this.getWorkers(j).stream().map(uuid -> String.format("%s%s%s",
                            ChatColor.WHITE,
                            job == null && this.isDead(uuid) ? ChatColor.GRAY : ChatColor.WHITE,
                            this.plugin.getName(uuid)
                    )).collect(Collectors.joining(ChatColor.WHITE + ",")));
            args.add(" ");
        });
        args.remove(args.size() - 1);
        args.add(ChatColor.DARK_GREEN + "================================");
        for (String str:args) player.sendMessage(str);
    }

    public void sendMessage(UUID uuid, String arg) {
        Optional<Player> player = this.plugin.getPlayer(uuid);
        if (player.isPresent()) {
            player.get().sendMessage(arg);
        } else {
            if (this.isAlive(uuid)) this.message_queue.get(uuid).add(arg);
        }
    }

    public void noticeMessage(String arg) {
        this.getPlayers().forEach(uuid -> sendMessage(uuid, arg));
        this.getSpectators().forEach(uuid -> sendMessage(uuid, arg));
    }

    public void noticeMessageAsTitle(String title, String sub) {
        this.getPlayers().forEach(uuid -> plugin.getPlayer(uuid).ifPresent(player -> player.sendTitle(title, sub)));
        this.getSpectators().forEach(uuid -> plugin.getPlayer(uuid).ifPresent(player -> player.sendTitle(title, sub)));
    }

    public void killSkeleton(Skeleton skeleton) {
        this.skeletons.remove(skeleton);
        if (!skeleton.isDead()) skeleton.remove();
    }


    private int[] getNumOfJobs(int pop) {
        //W,M,L,V,B,H,C
        if (this.plugin.getGameConfig().isReloaded()) {
            if (this.plugin.getGameConfig().getJobs().containsKey(pop)) {
                return this.plugin.getGameConfig().getJobs().get(pop);
            }
        }
        return switch (pop) {
            case 3, 4, 5 -> new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0};
            case 6, 7 -> new int[]{1, 1, 0, 0, 0, 0, 0, 0, 0};
            case 8, 9 -> new int[]{2, 1, 0, 0, 0, 0, 0, 0, 0};
            case 10, 11 -> new int[]{2, 1, 0, 1, 0, 0, 0, 0, 0};
            case 12, 13, 14 -> new int[]{3, 1, 0, 1, 0, 0, 0, 0, 0};
            case 15, 16 -> new int[]{3, 1, 0, 1, 0, 0, 1, 1, 0};
            default -> new int[]{4, 1, 0, 1, 0, 0, 1, 1, 0};
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
        if (!this.prayers.contains(uuid)) return false;
        this.prayers.remove(uuid);
        return true;
    }

    public boolean useTalisman(UUID uuid) {
        if (this.talisman.contains(uuid)) {
            this.sendMessage(uuid, JinroRPG.getChatLogo(ChatColor.RED) + "既に使用しています");
            return false;
        }
        this.talisman.add(uuid);
        this.sendMessage(uuid, String.format("%s %s を使用しました", JinroRPG.getChatLogo(ChatColor.RED), "天啓の呪符"));
        return true;
    }

    public boolean hasTalisman(UUID uuid) {
        return this.talisman.contains(uuid);
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
                                        player.sendMessage(JinroRPG.getChatLogo(ChatColor.RED) + " 残りの占い可能回数: " + heart.getOrDefault(player.getUniqueId(), 0) + "回");
                                    }
                                }
                        ));
            }
        }.runTask(this.plugin);
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
}
