package net.tagucha.jrpg.packet;

import net.tagucha.jrpg.PluginMain;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UndarkCore {
    private final static List<UUID> canceler = new ArrayList<>();

    public static void init(PluginMain plugin) {
        new NettyInjector(plugin);
    }

    public static List<UUID> getCanceler() {
        return canceler;
    }

    public static void addPlayer(UUID uuid) {
        canceler.add(uuid);
    }

    public static void removePlayer(UUID uuid) {
        canceler.remove(uuid);
    }

    public static void clearPlayer() {
        canceler.clear();
    }
}
