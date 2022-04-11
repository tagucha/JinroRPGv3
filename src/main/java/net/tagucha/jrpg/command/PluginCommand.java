package net.tagucha.jrpg.command;

import net.tagucha.jrpg.JinroRPG;
import net.tagucha.jrpg.exception.GameException;
import net.tagucha.jrpg.exception.PermissionException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginCommand implements TabExecutor, Listener {
    protected final JinroRPG plugin;
    protected final String command;

    public PluginCommand(JinroRPG plugin, String command) {
        this.plugin = plugin;
        this.command = command;
    }

    public void register() {
        org.bukkit.command.PluginCommand cmd = this.plugin.getCommand(this.command);
        if (cmd == null) {
            this.plugin.getLogger().info("Cannot load the command, " + this.command + ".");
        } else {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        return null;
    }

    public void checkPermission(CommandSender sender, String permission) throws GameException {
        if (permission == null) return;
        if (!sender.hasPermission(permission)) throw new PermissionException();
    }

    public void checkAndPut(CommandSender sender, List<String> commands, String command, String cmd, String permission) {
        if (permission != null) if (!sender.hasPermission(permission)) return;
        if (!command.contains(cmd)) return;;
        commands.add(command);
    }

    public boolean matchParameter(String[] strs,String[] args) {
        if (strs.length != args.length + 1) return false;
        for (int i = 0;i < args.length;i++) {
            if (args[i].equalsIgnoreCase("-")) continue;
            if (!strs[i].equalsIgnoreCase(args[i])) return false;
        }
        return true;
    }
}
