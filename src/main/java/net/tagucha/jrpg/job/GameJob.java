package net.tagucha.jrpg.job;

import org.bukkit.ChatColor;

public enum GameJob {
    VILLAGER("村人", "村人", ChatColor.GREEN, ChatColor.GREEN,' ',0,-1, true),
    WEREWOLF("人狼", "人狼", ChatColor.DARK_RED, ChatColor.DARK_RED,'w',1,0, false),
    MAD("共犯者", VILLAGER.fortune_name, ChatColor.GRAY, VILLAGER.fortune,'m',-1,1, true),
    LYCANTHROPY("狼憑き", VILLAGER.display_name, WEREWOLF.fortune_name, ChatColor.DARK_PURPLE, VILLAGER.display, WEREWOLF.fortune, 'l',0,2, false),
    VAMPIRE("吸血鬼", "吸血鬼", ChatColor.RED, ChatColor.RED,'v',2,3, false),
    BAKER("パン屋", VILLAGER.fortune_name, ChatColor.WHITE, VILLAGER.fortune,'b',0,4, true),
    HUNTER("狩人",VILLAGER.fortune_name, ChatColor.DARK_GREEN, VILLAGER.fortune,'h',0,5, true),
    CORONER("検死官", VILLAGER.fortune_name, ChatColor.GOLD, VILLAGER.fortune, 'c',0,6, true),
    SMART_WEREWOLF("賢狼", WEREWOLF.fortune_name, WEREWOLF.real, WEREWOLF.fortune, 's', 1,7,false),
    INSANE_WEREWOLF("狂狼", WEREWOLF.fortune_name, WEREWOLF.real, WEREWOLF.fortune, 'i', 1,8,false);

    private final String name;
    private final String display_name;
    private final String fortune_name;
    private final ChatColor real;
    private final ChatColor display;
    private final ChatColor fortune;
    public final char id;
    public final int side;
    public final int count;
    public final boolean isHuman;

    GameJob(String name, String fortune_name, ChatColor real, ChatColor fortune, char id, int side, int count, boolean isHuman) {
        this(name, name, fortune_name, real, real, fortune, id, side, count, isHuman);
    }

    GameJob(String name, String display_name, String fortune_name, ChatColor real, ChatColor display, ChatColor fortune, char id, int side, int count, boolean isHuman) {
        this.name = name;
        this.display_name = display_name;
        this.fortune_name = fortune_name;
        this.real = real;
        this.display = display;
        this.fortune = fortune;
        this.id = id;
        this.side = side;
        this.count = count;
        this.isHuman = isHuman;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return String.format("%s%s%s",this.display,ChatColor.BOLD,this.display_name);
    }

    public String getFortuneResult() {
        return String.format("%s%s%s",this.fortune,ChatColor.BOLD,this.fortune_name);
    }

    public String getRealName() {
        return String.format("%s%s%s",this.real,ChatColor.BOLD,this.name);
    }
}
