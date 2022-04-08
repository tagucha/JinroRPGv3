package net.tagucha.jrpg.config;

import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.job.GameJob;
import net.tagucha.jrpg.util.ItemUtil;
import net.tagucha.jrpg.item.GameItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GameConfig {
    private final PluginMain plugin;
    private FileConfiguration config;
    private final Map<String, Integer> custom_model_data = new HashMap<>();
    private final Map<Integer,int[]> jobs = new HashMap<>();
    private double skeleton_per_population = 10;
    private double emerald_odds = 0.5;
    private int time_day = 120,time_night = 120,time_first_day = 60;
    private int max_player = 16;
    private boolean isReloaded = false;

    public GameConfig(PluginMain plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        this.isReloaded = true;
        CustomConfig custom_config = new CustomConfig(this.plugin);
        custom_config.saveDefaultConfig();
        custom_config.reloadConfig();
        this.config = custom_config.getConfig();
        this.plugin.ITEMS.merchant_recipes.get(GameItem.ItemType.SUPPORT).clear();
        this.plugin.ITEMS.merchant_recipes.get(GameItem.ItemType.COMBAT).clear();
        this.plugin.ITEMS.items.forEach(item -> {
            GameItem.ItemType type = item.getConfigKey().type();
            String key = item.getConfigKey().key();
            String key_cmd = String.format("items.%s.%s.custom_model_data", type.key, key);
            String key_price = String.format("items.%s.%s.price", type.key, key);
            if (config.contains(key_price)) {
                if (config.isInt(key_price)) {
                    if (this.config.getInt(key_price) >= 1) {
                        if (type != GameItem.ItemType.OTHER) {
                            MerchantRecipe recipe = new MerchantRecipe(this.plugin.ITEMS.getItem(item), Integer.MAX_VALUE);
                            recipe.setIngredients(Collections.singletonList(new ItemStack(Material.EMERALD, this.config.getInt(key_price))));
                            recipe.setExperienceReward(false);
                            this.plugin.ITEMS.merchant_recipes.get(type).put(key, recipe);
                        }
                    } else {
                        this.log(String.format("%sが自然数でないためショップで販売されません",key_price));
                    }
                } else {
                    this.log(String.format("%sは整数である必要があります(デフォルトの値を使用します)",key_price));
                }
            }
            if (config.contains(key_cmd)) {
                if (config.isInt(key_cmd)) {
                    ItemUtil.setCMD(this.plugin.ITEMS.getItem(item), this.config.getInt(key_cmd));
                    this.custom_model_data.put(key,this.config.getInt(key_cmd));
                } else this.log(String.format("%sは整数である必要があります(デフォルトの値を使用します)",key_cmd));
            }
        });

        this.skeleton_per_population = this.config.getDouble("numbers.skeleton_per_population",10.0);
        if (this.skeleton_per_population < 0) {
            this.log("numbers.skeleton_per_populationは正の小数である必要があります(デフォルトの値を使用します)");
            this.skeleton_per_population = 10;
        }

        this.emerald_odds = this.config.getDouble("numbers.emerald_odds",0.5);
        if (this.emerald_odds < 0 || 1 < this.emerald_odds) {
            this.log("numbers.emerald_oddsはの0~1の小数である必要があります(デフォルトの値を使用します)");
            this.emerald_odds = 0.5;
        }

        this.time_day = this.config.getInt("numbers.time_day",120);
        if (this.time_day < 0 && this.config.isInt("numbers.time_day")) {
            this.log("numbers.time_dayは自然数である必要があります(デフォルトの値を使用します)");
            this.time_day = 120;
        }

        this.time_night = this.config.getInt("numbers.time_night",120);
        if (this.time_night < 0 && this.config.isInt("numbers.time_night")) {
            this.log("numbers.time_nightは自然数である必要があります(デフォルトの値を使用します)");
            this.time_night = 120;
        }

        this.time_first_day = this.config.getInt("numbers.time_first_day",60);
        if (this.time_first_day < 0 && this.config.isInt("numbers.time_first_day")) {
            this.log("numbers.time_first_dayは自然数である必要があります(デフォルトの値を使用します)");
            this.time_first_day = 60;
        }

        this.max_player = this.config.getInt("numbers.max_player",60);
        if (this.max_player < 3 && this.config.isInt("numbers.max_player")) {
            this.log("numbers.max_playerは3以上の自然数である必要があります(デフォルトの値を使用します)");
            this.max_player = 3;
        }

        if (this.config.contains("numbers.jobs")) {
            for (int p = 3;p <= this.max_player;p++) {
                if (this.config.contains(String.format("numbers.jobs.%d", p))) {
                    Properties prop = new Properties();
                    int sum = 0;
                    boolean suc = true;
                    for (String s : this.config.getString(String.format("numbers.jobs.%d", p)).split(",")) {
                        if (s.length() < 3) {
                            this.log(String.format("L<3: numbers.jobs.%dの%sは\"[JobID(アルファベット小文字1文字)]=数\"である必要はあります(デフォルトの値を使用します)", p, s));
                            suc = false;
                        } else {
                            if (s.charAt(1) != '=') {
                                this.log(String.format("Not2=,numbers.jobs.%dの%sは\"[JobID(アルファベット小文字1文字)]=数\"である必要はあります(デフォルトの値を使用します)", p, s));
                                suc = false;
                            } else {
                                char c = s.charAt(0);
                                try {
                                    int pop = Integer.parseInt(s.substring(2));
                                    sum += pop;
                                    prop.put(c, pop);
                                } catch (Exception e) {
                                    this.log(String.format("%s: numbers.jobs.%dの%sは\"[JobID(アルファベット小文字1文字)]=数\"である必要はあります(デフォルトの値を使用します)", e.getClass().getSimpleName(), p, s));
                                    suc = false;
                                }
                            }
                        }
                    }
                    if (suc) {
                        if (sum > this.max_player) {
                            this.log(String.format("numbers.jobs.%dの合計はnumbers.max_player以下である必要があります(デフォルトの値を使用します)", p));
                        } else {
                            int[] job_pop = new int[GameJob.values().length - 1];
                            for (GameJob job:GameJob.values()) {
                                if (job.count >= 0) job_pop[job.count] = (int) prop.getOrDefault(job.id, 0);
                            }
                            this.jobs.put(p,job_pop);
                        }
                    }
                }
            }
        }
    }

    public Map<String, Integer> getCustomModelData() {
        return custom_model_data;
    }

    public double getSkeletonPerPopulation() {
        return skeleton_per_population;
    }

    public double getEmeraldOdds() {
        return emerald_odds;
    }

    public int getTimeDay() {
        return time_day;
    }

    public int getTimeNight() {
        return time_night;
    }

    public int getTimeFirstDay() {
        return time_first_day;
    }

    public int getMaxPlayers() {
        return max_player;
    }

    public Map<Integer, int[]> getJobs() {
        return jobs;
    }

    public boolean isReloaded() {
        return isReloaded;
    }

    private void log(String message) {
        this.plugin.getLogger().info(String.format("[GameConfig] %s",message));
    }
}
