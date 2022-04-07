package net.tagucha.jrpg;

import net.tagucha.jrpg.event.GameTickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import net.tagucha.jrpg.event.ChangeToDayEvent;
import net.tagucha.jrpg.event.ChangeToNightEvent;

public class GameTimer {
    public final int max_time_night;
    public final int max_time_day;
    private final PluginMain plugin;
    private final JinroGame game;
    private final BossBar time_bar;
    private long total_time = 0;
    private int time;
    private int day = 1;
    private boolean isStarted = false;
    private boolean isStopped = false;
    private Clock clock = Clock.DAY;
    private TimerThread nextThread = null;

    private static final String DAY_TIME = String.format("%s%sDAY TIME",ChatColor.GOLD,ChatColor.BOLD);
    private static final String NIGHT_TIME = String.format("%s%sNIGHT TIME",ChatColor.BLUE,ChatColor.BOLD);

    private static final BarColor DAY_COLOR = BarColor.YELLOW;
    private static final BarColor NIGHT_COLOR = BarColor.PURPLE;

    public GameTimer(PluginMain plugin,JinroGame game) {
        this.plugin = plugin;
        this.game = game;
        this.max_time_night = plugin.getGameConfig().getTimeNight();
        this.max_time_day = plugin.getGameConfig().getTimeDay();
        this.time = plugin.getGameConfig().getTimeFirstDay();
        this.time_bar = Bukkit.createBossBar(DAY_TIME,DAY_COLOR, BarStyle.SEGMENTED_6);
    }

    public void start() {
        this.isStarted = true;
        new TimerThread().runTaskLater(this.plugin,20);
        this.game.getWorld().setTime(6000);
    }

    public void stop() {
        this.isStopped = true;
        this.nextThread.cancel();
        this.time_bar.removeAll();
        this.game.getWorld().setTime(6000);
    }

    public boolean isStopped() {
        return isStopped;
    }

    public JinroGame getGame() {
        return game;
    }

    public Clock getClock() {
        return clock;
    }

    private void next() {
        this.time--;
        this.total_time++;
        GameTickEvent event = new GameTickEvent(game);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (this.time <= 0) {
            switchClock();
        }
        this.updateTimeBar();
    }

    public long getTotalTime() {
        return this.total_time;
    }

    public boolean isDay() {
        return this.clock == Clock.DAY;
    }

    public boolean isNight() {
        return this.clock == Clock.NIGHT;
    }

    public int getDay() {
        return day;
    }

    private void switchClock() {
        if (this.clock == Clock.DAY) {
            this.clock = Clock.NIGHT;
            this.time_bar.setTitle(NIGHT_TIME);
            this.time_bar.setColor(NIGHT_COLOR);
            this.time = this.max_time_night;
            this.game.getWorld().setTime(18000);
            Bukkit.getPluginManager().callEvent(new ChangeToNightEvent(this.game));
        }
        else {
            this.clock = Clock.DAY;
            this.time_bar.setTitle(DAY_TIME);
            this.time_bar.setColor(DAY_COLOR);
            this.time = this.max_time_day;
            this.game.getWorld().setTime(6000);
            this.day++;
            Bukkit.getPluginManager().callEvent(new ChangeToDayEvent(this.game));
        }
    }

    private void updateTimeBar() {
        this.game.getWorld().getPlayers().forEach(this.time_bar::addPlayer);
        this.time_bar.setProgress(((double) time) / ((double) (this.clock == Clock.DAY ? max_time_day : max_time_night)));
    }

    private class TimerThread extends BukkitRunnable {
        @Override
        public void run() {
            GameTimer.this.next();
            GameTimer.this.nextThread = new TimerThread();
            GameTimer.this.nextThread.runTaskLater(GameTimer.this.plugin,20);
        }
    }

    public enum Clock {
        DAY,
        NIGHT
    }
}
