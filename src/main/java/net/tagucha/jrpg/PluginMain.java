package net.tagucha.jrpg;

import io.netty.channel.Channel;
import net.tagucha.jrpg.command.GameCommand;
import net.tagucha.jrpg.config.CustomConfig;
import net.tagucha.jrpg.config.GameConfig;
import net.tagucha.jrpg.event.OriginalEventManager;
import net.tagucha.jrpg.item.GameItems;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.tagucha.jrpg.packet.UndarkCore;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class PluginMain extends JavaPlugin implements Listener {
    private JinroGame GAME = null;
    public GameItems ITEMS;
    public GameMerchant MERCHANT;
    private final Map<UUID,Player> uuidToPlayer = new HashMap<>();
    private final HashSet<Player> onlinePlayers = new HashSet<>();
    private final HashSet<UUID> onlinePLayersUUID = new HashSet<>();
    private final Map<UUID, String> uuidToName = new HashMap<>();
    private final GameConfig config = new GameConfig(this);

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        this.ITEMS = new GameItems(this);
        this.MERCHANT = new GameMerchant(this);
        this.getServer().getPluginManager().registerEvents(this,this);
        this.getServer().getPluginManager().registerEvents(new JinroGame.GameListener(this),this);
        this.getServer().getOnlinePlayers().forEach(this::registerPlayer);
        new OriginalEventManager(this).init();
        this.config.reload();
        new GameCommand(this).register();
        new CustomConfig(this, "world_setting_sample").saveDefaultConfig();
        UndarkCore.init(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.registerPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.unregisterPlayer(event.getPlayer());
    }

    public void registerPlayer(Player player) {
        this.uuidToPlayer.put(player.getUniqueId(),player);
        this.uuidToName.put(player.getUniqueId(),player.getName());
        this.onlinePlayers.add(player);
        this.onlinePLayersUUID.add(player.getUniqueId());
    }

    public void unregisterPlayer(Player player) {
        this.uuidToPlayer.remove(player.getUniqueId());
        this.onlinePlayers.remove(player);
        this.onlinePLayersUUID.remove(player.getUniqueId());
    }

    public HashSet<Player> getOnlinePlayers() {
        return new HashSet<>(onlinePlayers);
    }

    public Optional<Player> getPlayer(UUID uuid) {
        return this.uuidToPlayer.containsKey(uuid) ? Optional.of(this.uuidToPlayer.get(uuid)) : Optional.empty();
    }

    public String getName(UUID uuid) {
        return this.uuidToName.getOrDefault(uuid,"-NULL-");
    }

    public Optional<JinroGame> isPlayer(UUID uuid) {
        if (this.GAME == null) return Optional.empty();
        if (!this.GAME.getPlayers().contains(uuid)) return Optional.empty();
        return Optional.of(this.GAME);
    }

    public boolean isOnline(UUID uuid) {
        return this.onlinePLayersUUID.contains(uuid);
    }

    public Optional<JinroGame> getJinroGame() {
        return Optional.ofNullable(this.GAME);
    }

    public boolean registerJinroGame(JinroGame game) {
        if (this.GAME != null) return false;
        this.GAME = game;
        return true;
    }

    public void unregisterJinroGame() {
        this.GAME = null;
    }

    public void noticeMessage(String message) {
        for (Player player: this.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public void noticeMessage(String message, ClickEvent click, HoverEvent hover) {
        for (Player player: this.getOnlinePlayers()) {
            this.sendMessage(player,message,click,hover);
        }
    }

    public void noticeMessage(Set<UUID> uuids, String message, ClickEvent click, HoverEvent hover) {
        uuids.forEach(uuid -> this.getPlayer(uuid).ifPresent(player -> this.sendMessage(player, message, click, hover)));
    }

    public void sendMessage(Player player,String message, ClickEvent click, HoverEvent hover) {
        TextComponent component = new TextComponent(message);
        if (click != null) component.setClickEvent(click);
        if (hover != null) component.setHoverEvent(hover);
        player.spigot().sendMessage(component);
    }

    public void sendMessageWithCommand(Player player,String message, String command, String arg) {
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND,command);
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder(arg).create());
        sendMessage(player,message,click,hover);
    }

    public GameConfig getGameConfig() {
        return config;
    }

    public static String getLogo(ChatColor color) {
        return String.format("%s[人狼RPG]%s",color,ChatColor.RESET);
    }
}