package net.tagucha.jrpg.command;

import net.tagucha.jrpg.core.GameManager;
import net.tagucha.jrpg.core.GamePreparation;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.event.GameEndEvent;
import net.tagucha.jrpg.exception.GameException;
import net.tagucha.jrpg.exception.SimpleMessageException;
import net.tagucha.jrpg.item.GameItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GameCommand extends PluginCommand {
    private GamePreparation preparation = null;

    public GameCommand(PluginMain plugin) {
        super(plugin, "jinro");
    }

    public Optional<GamePreparation> getPreparation() {
        return Optional.ofNullable(this.preparation);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        boolean isPlayer = sender instanceof Player;
        try {
            switch (args.length) {
                case 0:
                    sendHelp(sender);
                    break;
                case 1:
                    if (args[0].equalsIgnoreCase("start")) {
                        if (!isPlayer) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " プレイヤーのみしか実行できません。"));
                            break;
                        }
                        this.checkPermission(sender,"jinro.command.start");
                        this.createGamePreparation(((Player) sender).getWorld(), 100).start();
                    } else if (args[0].equalsIgnoreCase("join")) {
                        if (!isPlayer) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " プレイヤーのみしか実行できません。"));
                            break;
                        }
                        this.checkPermission(sender,null);
                        this.getPreparation().ifPresent(prep -> prep.addPlayer((Player) sender));
                    } else if (args[0].equalsIgnoreCase("cancel")) {
                        this.checkPermission(sender, "jinro.command.cancel");
                        if (this.getPreparation().isEmpty()) sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " ゲームは開始されていません。"));
                        else {
                            this.plugin.noticeMessage(PluginMain.getLogo(ChatColor.GOLD) + " ゲームはキャンセルされました。");
                            this.getPreparation().get().cancelGame();
                            this.preparation = null;
                        }
                    }
                    break;
                case 2:
                    if (args[0].equalsIgnoreCase("start")) {
                        if (!isPlayer) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " プレイヤーのみしか実行できません。"));
                            break;
                        }
                        this.checkPermission(sender,"jinro.command.start");
                        try {
                            int time = Integer.parseInt(args[1]);
                            this.createGamePreparation(((Player) sender).getWorld(), time).start();
                        } catch (NumberFormatException e) {
                            throw new SimpleMessageException(ChatColor.RED,ChatColor.RED,"数値がおかしいです。");
                        }
                    } else if (args[0].equalsIgnoreCase("spawn")) {
                        if (!isPlayer) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " プレイヤーのみしか実行できません。"));
                            break;
                        }
                        this.checkPermission(sender, "jinro.command.spawn");
                        if (args[1].equalsIgnoreCase("combat") || args[1].equalsIgnoreCase("戦闘")) {
                            this.plugin.MERCHANT.spawnCombatMerchant(((Player) sender).getLocation());
                        } else if (args[1].equalsIgnoreCase("support") || args[1].equalsIgnoreCase("補助")) {
                            this.plugin.MERCHANT.spawnSupportMerchant(((Player) sender).getLocation());
                        } else {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " 第2引数は[combat|support]のみです。"));
                            break;
                        }
                    } else if (args[0].equalsIgnoreCase("give")) {
                        if (!isPlayer) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " プレイヤーのみしか実行できません。"));
                            break;
                        }
                        this.checkPermission(sender, "jinro.command.give");
                        Optional<GameItem> opt = this.plugin.ITEMS.items.stream().filter(item -> item.getConfigKey().key.equalsIgnoreCase(args[1])).findAny();
                        if (opt.isPresent()) ((Player) sender).getInventory().addItem(this.plugin.ITEMS.getItem(opt.get()));
                        else sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " そのようなアイテムは存在しません。"));
                    } else if (args[0].equalsIgnoreCase("sign")) {
                        if (!isPlayer) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " プレイヤーのみしか実行できません。"));
                            break;
                        }
                        this.checkPermission(sender, "jinro.command.sign");
                        try {
                            Material material = Material.getMaterial(args[1]);
                            if (!GameManager.isSign(material)){
                                sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " 素材は看板を指定してください。"));
                                return true;
                            }
                            ItemStack item = new ItemStack(material);
                            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                            meta.setDisplayName(ChatColor.GOLD + "未登録の看板");
                            Sign sign = (Sign) meta.getBlockState();
                            sign.setLine(1, GameManager.REGISTER_YET);
                            sign.setLine(3, GameManager.TO_REGISTER);
                            meta.setBlockState(sign);
                            item.setItemMeta(meta);
                            ((Player) sender).getInventory().addItem(item);
                        } catch (NullPointerException e) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " 素材は看板を指定してください。"));
                        }
                    }
                    break;
                case 3:
                    if (args[0].equalsIgnoreCase("sign")) {
                        if (!isPlayer) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " プレイヤーのみしか実行できません。"));
                            break;
                        }
                        this.checkPermission(sender, "jinro.command.sign");
                        try {
                            Material material = Material.getMaterial(args[1]);
                            if (!GameManager.isSign(material)){
                                sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " 素材は看板を指定してください。"));
                                return true;
                            }
                            ItemStack item = new ItemStack(material);
                            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                            meta.setDisplayName(ChatColor.GOLD + "登録済みの看板(" + args[2] + ")");
                            Sign sign = (Sign) meta.getBlockState();
                            sign.setLine(1, GameManager.REGISTERED.replace("<NAME>",args[2]));
                            sign.setLine(3, GameManager.TO_FORTUNE);
                            meta.setBlockState(sign);
                            item.setItemMeta(meta);
                            ((Player) sender).getInventory().addItem(item);
                        } catch (NullPointerException e) {
                            sender.sendMessage(String.format("%s%s", PluginMain.getLogo(ChatColor.RED), " 素材は看板を指定してください。"));
                        }
                    }
                    break;
            }
        } catch (GameException e) {
            sender.sendMessage(e.getMessage());
            System.out.println(this.preparation);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 1:
                this.checkAndPut(sender,list,"start",args[0],"jinro.command.start");
                this.checkAndPut(sender,list,"cancel",args[0],"jinro.command.cancel");
                this.checkAndPut(sender,list,"spawn",args[0],"jinro.command.spawn");
                this.checkAndPut(sender,list,"give",args[0],"jinro.command.give");
                this.checkAndPut(sender,list,"sign",args[0],"jinro.command.sign");
                this.checkAndPut(sender,list,"join", args[0], null);
                break;
            case 2:
                if (this.matchParameter(args,new String[]{"spawn"})) {
                    this.checkAndPut(sender,list,"combat",args[1],"jinro.command.spawn");
                    this.checkAndPut(sender,list,"support",args[1],"jinro.command.spawn");
                } else if (this.matchParameter(args, new String[]{"give"})) {
//                    Arrays.stream(GameItems.class.getFields()).filter(field -> field.getType().equals(ItemStack.class)).map(Field::getName).forEach(arg -> this.checkAndPut(sender, list, arg, args[1],"jinro.command.give"));
                    this.plugin.ITEMS.items.stream().map(item -> item.getConfigKey().key.toUpperCase()).forEach(arg -> this.checkAndPut(sender, list, arg, args[1],"jinro.command.give"));
                } else if (this.matchParameter(args, new String[]{"sign"})) {
                    this.checkAndPut(sender,list,"SPRUCE_SIGN",args[1],"jinro.command.sign");
                    this.checkAndPut(sender,list,"ACACIA_SIGN",args[1],"jinro.command.sign");
                    this.checkAndPut(sender,list,"BIRCH_SIGN",args[1],"jinro.command.sign");
                    this.checkAndPut(sender,list,"DARK_OAK_SIGN",args[1],"jinro.command.sign");
                    this.checkAndPut(sender,list,"JUNGLE_SIGN",args[1],"jinro.command.sign");
                    this.checkAndPut(sender,list,"OAK_SIGN",args[1],"jinro.command.sign");
                    this.checkAndPut(sender,list,"CRIMSON_SIGN",args[1],"jinro.command.sign");
                    this.checkAndPut(sender,list,"WARPED_SIGN",args[1],"jinro.command.sign");
                }
                break;
            case 3:
                if (this.matchParameter(args,new String[]{"sign","-"})) {
                    Bukkit.getOnlinePlayers().stream().map(Player::getDisplayName).forEach(arg -> this.checkAndPut(sender, list, arg, args[2],"jinro.command.sign"));
                }
        }
        return list;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(String.format("%s==============Command List==============", ChatColor.GOLD));
        sender.sendMessage(String.format("%s/jinro",ChatColor.WHITE));
        if (sender.hasPermission("jinro.command.start")) sender.sendMessage(String.format("%s- start [<integer>] : ゲームを開始します",ChatColor.WHITE));
        if (sender.hasPermission("jinro.command.cancel")) sender.sendMessage(String.format("%s- cancel : ゲームを開始します",ChatColor.WHITE));
        sender.sendMessage(String.format("%s- join : 開催されてるゲームに参加します",ChatColor.WHITE));
        if (sender.hasPermission("jinro.command.spawn")) sender.sendMessage(String.format("%s- spawn <combat|support> : 村人を召喚します",ChatColor.WHITE));
        if (sender.hasPermission("jinro.command.give")) sender.sendMessage(String.format("%s- give <itemID>", ChatColor.WHITE));
        if (sender.hasPermission("jinro.command.sign")) sender.sendMessage(String.format("%s- sign <material> [<player name>]", ChatColor.WHITE));
        sender.sendMessage(String.format("%s========================================", ChatColor.GOLD));
    }

    public GamePreparation createGamePreparation(World world, int time) throws GameException {
        if (this.getPreparation().isPresent()) throw new SimpleMessageException(ChatColor.RED,ChatColor.RED,"ゲームは既に開始されています。");
        this.preparation = new GamePreparation(this.plugin, world, time);
        return this.getPreparation().get();
    }

    @EventHandler
    public void onFinishGame(GameEndEvent event) {
        this.preparation = null;
    }
}
