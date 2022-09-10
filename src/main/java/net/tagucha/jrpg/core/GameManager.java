package net.tagucha.jrpg.core;

import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.level.EntityPlayer;
import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.event.*;
import net.tagucha.jrpg.job.GameJob;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftChatMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public record GameManager(JinroRPG plugin) implements Listener {
    public static final String REGISTER_YET = String.format("%s%s未登録", ChatColor.DARK_BLUE, ChatColor.BOLD);
    public static final String TO_REGISTER = String.format("%s<%s登録 %s: %s右クリック%s>", ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.BLACK);
    public static final String REGISTERED = String.format("%s%s%s<NAME>", ChatColor.DARK_BLUE, ChatColor.BOLD, ChatColor.UNDERLINE);
    public static final String TO_FORTUNE = String.format("%s<%s占う %s: %s右クリック%s>", ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.BLACK);

    @EventHandler
    public void onTick(GameTickEvent event) {
        event.getGame().getAlive().stream().map(uuid -> plugin().getPlayer(uuid)).filter(Optional::isPresent).map(Optional::get).forEach(player -> {
            String message = String.format("%s%s占い可能回数%s: %d回",ChatColor.DARK_BLUE, ChatColor.BOLD, ChatColor.WHITE, event.getGame().getHeart().getOrDefault(player.getUniqueId(), 0));
            ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(CraftChatMessage.fromStringOrNull(message));
            ((CraftPlayer) player).getHandle().b.a(packet);
        });
    }

    @EventHandler
    public void onStart(GameStartEvent event) {
        this.plugin.MERCHANT.respawn(event.getGame().getWorld());
        Set<UUID> werewolf_side = new HashSet<>();
        for (UUID uuid:event.getGame().getPlayers()) {
            if (event.getGame().getJob(uuid).filter(gameJob -> gameJob.side == 1).isPresent()) werewolf_side.add(uuid);
            this.plugin.getPlayer(uuid).ifPresent(player -> player.sendTitle("あなたの役職 ： " + event.getGame().getJob(uuid).get().getRealName(),
                    ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "GAME START"));
        }
        String names = werewolf_side.stream().map(this.plugin::getName).collect(Collectors.joining(", "));
        werewolf_side.forEach(uuid -> event.getGame().sendMessage(uuid, JinroRPG.getChatLogo(ChatColor.GOLD) + " 人狼は " + names + "です"));
    }

    @EventHandler
    public void onEnd(GameEndEvent event) {
        this.plugin.unregisterJinroGame();
        for (UUID uuid:event.getGame().getPlayers()) {
            this.plugin.getPlayer(uuid).ifPresent(player -> {
                final EntityPlayer joiner = ((CraftPlayer) player).getHandle();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player p:Bukkit.getOnlinePlayers()) {
                            joiner.b.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, ((CraftPlayer)p).getHandle()));
                        }
                    }
                }.runTask(this.plugin);
            });
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
        if (optional.isPresent()) {
            JinroGame game = optional.get();
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent evt) {
                if (evt.getDamager() instanceof Player attacker) {
                    GamePlayerKillPlayerEvent called_event = new GamePlayerKillPlayerEvent(game, attacker.getUniqueId(), player.getUniqueId());
                    Bukkit.getPluginManager().callEvent(called_event);
                } else if (evt.getDamager() instanceof Projectile projectile) {
                    if (projectile.getShooter() instanceof Player attacker) {
                        GamePlayerKillPlayerEvent called_event = new GamePlayerKillPlayerEvent(game, attacker.getUniqueId(), player.getUniqueId());
                        Bukkit.getPluginManager().callEvent(called_event);
                    }
                }
            }
            if (game.isAlive(player.getUniqueId())) {
                game.die(player.getUniqueId());
                game.getSpectators().forEach(uuid -> game.sendMessage(uuid, event.getDeathMessage()));
                game.getDead().forEach(uuid -> game.sendMessage(uuid, event.getDeathMessage()));
                event.setDeathMessage(null);
                event.getDrops().clear();
                final EntityPlayer joiner = ((CraftPlayer) player).getHandle();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player p:Bukkit.getOnlinePlayers()) {
                            joiner.b.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, ((CraftPlayer)p).getHandle()));
                        }
                    }
                }.runTask(this.plugin);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Optional<JinroGame> opt = this.plugin.isPlayer(player.getUniqueId());
        if (opt.isPresent()) {
            final JinroGame game = opt.get();
            game.getArea().getSpawnPoint().ifPresent(event::setRespawnLocation);
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }.runTaskLater(this.plugin, 1);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.plugin.isPlayer(player.getUniqueId()).ifPresent(game -> {
            if (game.isAlive(player.getUniqueId())) {
                player.sendMessage(JinroRPG.getChatLogo(ChatColor.AQUA) + " あなたの役職は" + game.getJob(player.getUniqueId()).get().getDisplayName());
                if (game.getMessageQueue().get(player.getUniqueId()).isEmpty()) {
                    player.sendMessage(JinroRPG.getChatLogo(ChatColor.AQUA) + " ログアウト中に届いたメッセージはありません。");
                } else {
                    player.sendMessage(JinroRPG.getChatLogo(ChatColor.AQUA) + " ログアウト中に以下のメッセージが届きました");
                    while (!game.getMessageQueue().get(player.getUniqueId()).isEmpty())
                        player.sendMessage(game.getMessageQueue().get(player.getUniqueId()).remove());
                    player.sendMessage(JinroRPG.getChatLogo(ChatColor.AQUA) + " 以上です。");
                }
                game.getLogoutSchedule().remove(player.getUniqueId()).cancel();
            } else if (game.getDead().contains(player.getUniqueId())) {
                event.setJoinMessage(null);
            }
        });
        final EntityPlayer joiner = ((CraftPlayer)event.getPlayer()).getHandle();
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getJinroGame().ifPresent(game -> {
                    for (UUID uuid:game.getAlive()) {
                        if (uuid.equals(event.getPlayer().getUniqueId())) continue;
                        plugin.getPlayer(uuid).ifPresent(living -> ((CraftPlayer)living).getHandle().b.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, joiner)));
                    }
                });
            }
        }.runTaskLater(this.plugin, 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        this.plugin.isPlayer(uuid).ifPresent(game -> {
            if (game.isAlive(uuid)) {
                event.setQuitMessage(String.format("%s %sがゲームから退出しました。一定時間が経過すると死亡判定がくだされます。", JinroRPG.getChatLogo(ChatColor.RED), event.getPlayer().getName()));
                game.getLogoutCount().put(uuid, game.getLogoutCount().getOrDefault(uuid, 0) + 1);
                int count = game.getLogoutCount().get(uuid);
                plugin.getLogger().info(JinroRPG.getChatLogo(ChatColor.RED) + " ログアウトカウント: " + count);
                long limit = (long) (120 - Math.pow(count - 1, 3));
                if (limit <= 0) game.die(uuid);
                else {
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            game.die(uuid);
                            game.getLogoutSchedule().remove(uuid);
                            game.noticeMessage(JinroRPG.getChatLogo(ChatColor.GOLD) + " " + event.getPlayer().getName() + "はログアウト時間超過のため死亡しました");
                        }
                    };
                    runnable.runTaskLater(this.plugin, limit * 20);
                    game.getLogoutSchedule().put(uuid, runnable);
                }
            } else if (game.getDead().contains(uuid)) {
                event.setQuitMessage(null);
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
    public void onChangeToDay(GameChangeToDayEvent event) {
        event.getGame().skeletons.forEach(skeleton -> {
            if (skeleton != null) skeleton.remove();
        });
        event.getGame().getPrayers().clear();
        event.getGame().getTalisman().clear();
        event.getGame().getAxeUsed().clear();
        event.getGame().noticeMessage(JinroRPG.getChatLogo(ChatColor.GOLD) + " 昼になりました");
        event.getGame().noticeMessageAsTitle(ChatColor.GOLD + "☼" + ChatColor.YELLOW + "昼になりました" + ChatColor.GOLD + "☼",event.getGame().timer.getDay() + "日目");
    }

    @EventHandler
    public void onChangeToNight(GameChangeToNightEvent event) {
        event.getGame().spawnSkeletons();
        event.getGame().noticeMessage(JinroRPG.getChatLogo(ChatColor.GOLD) + " 夜になりました");
        event.getGame().noticeMessageAsTitle(ChatColor.YELLOW + "" + ChatColor.BOLD + "☽" + ChatColor.RESET + ChatColor.DARK_BLUE + "夜になりました" + ChatColor.YELLOW + "" + ChatColor.BOLD + "☽","");
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
                                event.getPlayer().sendMessage(JinroRPG.getChatLogo(ChatColor.RED) + " 既に他の看板に登録しています");
                                return;
                            }
                        }
                sign.setLine(1, REGISTERED.replace("<NAME>", event.getPlayer().getName()));
                sign.setLine(3, TO_FORTUNE);
                sign.update();
                event.getPlayer().sendMessage(JinroRPG.getChatLogo(ChatColor.RED) + " 看板を登録しました");
                return;
            }
            Optional<JinroGame> optional = this.plugin.isPlayer(player.getUniqueId());
            if (optional.isPresent() && !player.getInventory().getItemInMainHand().isSimilar(this.plugin.ITEMS.PROVIDENCE_OF_KNIGHT) && !player.getInventory().getItemInMainHand().isSimilar(this.plugin.ITEMS.TELESCOPE)) {
                JinroGame game = optional.get();
                for (UUID uuid : game.getPlayers()) {
                    if (!(sign.getLine(1).equalsIgnoreCase("§1§l§n" + this.plugin.getName(uuid)) && sign.getLine(3).equalsIgnoreCase(TO_FORTUNE)))
                        continue;
                    if (event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                        if (game.getAlive().contains(event.getPlayer().getUniqueId())) if (game.timer.isDay()) {
                            event.getPlayer().sendMessage(JinroRPG.getChatLogo(ChatColor.RED) + " 占いは夜のみ可能です");
                            return;
                        }
                        if (!game.fortune(event.getPlayer().getUniqueId())) {
                            event.getPlayer().sendMessage(JinroRPG.getChatLogo(ChatColor.RED) + " 占い可能回数が0です");
                            return;
                        }
                    }
                    String arg = game.getJob(uuid).get().getFortuneResult();
                    if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                        if (game.getJob(uuid).isPresent()) if (game.getJob(uuid).get() == GameJob.MAD)
                            arg += ChatColor.GRAY + "(" + GameJob.MAD.getRealName() + ")";
                        if (game.getDead().contains(uuid)) arg += ChatColor.GRAY + "(死亡)";
                    }
                    event.getPlayer().sendMessage(String.format("%s占い結果: %sは%s", JinroRPG.getChatLogo(ChatColor.RED), this.plugin.getName(uuid), arg));
                    if (game.isAlive(player.getUniqueId())) {
                        player.sendMessage(JinroRPG.getChatLogo(ChatColor.RED) + " 残りの占い可能回数: " + game.getHeart().getOrDefault(player.getUniqueId(), 0) + "回");
                        Bukkit.getPluginManager().callEvent(new GameFortuneEvent(game, event.getPlayer(), uuid));
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onFortune(GameFortuneEvent event) {
        if (event.getGame().hasTalisman(event.getTarget()))
            event.getGame().sendMessage(event.getTarget(), String.format("%s[天啓の呪符] %sあなたは占われました", ChatColor.GRAY, ChatColor.RED));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            this.plugin.isPlayer(uuid).ifPresent(game -> {
                if (game.useInvincible(uuid)) {
                    if (player.getHealth() <= event.getDamage()) {
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
                    event.getPlayer().sendMessage(String.format("%s 夜間にチャットは使えません", JinroRPG.getChatLogo(ChatColor.RED)));
                    return;
                }
                String message = String.format("%s %s: %s", JinroRPG.getChatLogo(ChatColor.DARK_AQUA), event.getPlayer().getName(), event.getMessage());
                this.plugin.getOnlinePlayers().forEach(player -> player.sendMessage(message));
            } else {
                String message = String.format("%s %s: %s", JinroRPG.getChatLogo(ChatColor.GRAY), event.getPlayer().getName(), event.getMessage());
                this.plugin.getOnlinePlayers().forEach(player -> {
                    if (!game.getAlive().contains(player.getUniqueId()))
                        game.sendMessage(player.getUniqueId(), message);
                });
            }
        });
    }

    public static boolean isSign(Material material) {
        if (material == null) return false;
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
