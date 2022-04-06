package net.tagucha.jrpg.config;

import net.tagucha.jrpg.PluginMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class CustomConfig {
    protected FileConfiguration config = null;
    protected final File configFile;
    protected final String file;
    protected final PluginMain plugin;

    public CustomConfig(PluginMain plugin) {
        this(plugin, "config");
    }

    public CustomConfig(PluginMain plugin, String fileName) {
        this(plugin,null,fileName);
    }

    public CustomConfig(PluginMain plugin, String path, String file) {
        this.plugin = plugin;
        if (path != null) this.file = path + "/" + file + ".yml";
        else this.file = file + ".yml";
        this.configFile = new File(plugin.getDataFolder(), this.file);
    }

    public boolean isExist() {
        return this.configFile.exists();
    }

    public String getName() {
        return this.file;
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(file, false);
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        final InputStream defConfigStream = plugin.getResource(file);
        if (defConfigStream == null) {
            return;
        }

        config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }
}
