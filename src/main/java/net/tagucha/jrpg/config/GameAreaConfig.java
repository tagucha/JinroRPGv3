package net.tagucha.jrpg.config;

import net.minecraft.util.Tuple;
import net.tagucha.jrpg.JinroGame;
import net.tagucha.jrpg.PluginMain;
import net.tagucha.jrpg.exception.GameAreaInputException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GameAreaConfig {
    private final PluginMain plugin;
    private final Random random;
    private final World world;
    private final JinroGame game;
    private final FileConfiguration config;
    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, Integer> count = new HashMap<>();
    private final Map<Integer, Location> map = new HashMap<>();
    private final int number;

    public GameAreaConfig(PluginMain plugin, JinroGame game, World world) throws GameAreaInputException {
        this.plugin = plugin;
        this.random = new Random();
        this.world = world;
        this.game = game;
        CustomConfig conf = new CustomConfig(plugin, "area", world.getName());
        if (!conf.isExist()) {
            StringBuilder builder = new StringBuilder();
            builder.append("area:").append('\n');
            try {
                conf.configFile.getParentFile().mkdirs();
                conf.configFile.createNewFile();
                BufferedWriter br = new BufferedWriter(new FileWriter(conf.configFile));
                br.write(builder.toString());
                br.close();
            } catch (IOException ignored) {}
            throw new GameAreaInputException(game,String.format("エリア設定ファイル(%s)がないんだが?",conf.getName()));
        }
        this.config = conf.getConfig();
        if (!config.contains("area"))  throw new GameAreaInputException(game,String.format("エリア設定ファイル(%s)の中にareaがないんだが?",config.getName()));
        for (String key : config.getConfigurationSection("area").getKeys(false)) {
            Shape shape = Shape.parseShape(config.getString(String.format("area.%s.shape",key),"NULL"));
            Type type = Type.parseShape(config.getString(String.format("area.%s.type",key),"ADD"));
            if (shape == Shape.NULL) throw new GameAreaInputException(game,String.format("エリア設定ファイル(%s)の中にarea.%s.shapeがおかしいんだが?",config.getName(),key));
            ShapedGameArea area;
            if (shape == Shape.BOX) {
                if (!config.contains(String.format("area.%s.max",key))) throw new GameAreaInputException(game,String.format("エリア設定ファイル(%s)がarea.%s.maxないんだが?",config.getName(),key));
                if (!config.contains(String.format("area.%s.min",key))) throw new GameAreaInputException(game,String.format("エリア設定ファイル(%s)がarea.%s.minないんだが?",config.getName(),key));
                int minX = Location.locToBlock(getDouble(String.format("area.%s.min.x",key)));
                int minY = Location.locToBlock(getDouble(String.format("area.%s.min.y",key)));
                int minZ = Location.locToBlock(getDouble(String.format("area.%s.min.z",key)));
                int maxX = Location.locToBlock(getDouble(String.format("area.%s.max.x",key)));
                int maxY = Location.locToBlock(getDouble(String.format("area.%s.max.y",key)));
                int maxZ = Location.locToBlock(getDouble(String.format("area.%s.max.z",key)));
                area = new BoxGameArea(minX, minY, minZ, maxX, maxY, maxZ);
            } else {
                if (!config.contains(String.format("area.%s.center",key))) throw new GameAreaInputException(game,String.format("エリア設定ファイル(%s)がarea.%s.centerないんだが?",config.getName(),key));
                int x = Location.locToBlock(getDouble(String.format("area.%s.center.x",key)));
                int y = Location.locToBlock(getDouble(String.format("area.%s.center.y",key)));
                int z = Location.locToBlock(getDouble(String.format("area.%s.center.z",key)));
                int r = Location.locToBlock(getDouble(String.format("area.%s.radius",key)));
                int h = Location.locToBlock(getDouble(String.format("area.%s.height",key)));
                area = new CylinderGameArea(x, y, z, r, h);
            }
            List<Location> locs = new ArrayList<>(area.getSpawnableLocations());
            locations.addAll(area.getSpawnableLocations());
            if (type == Type.PILE) locs.forEach(loc->this.count.put(loc,this.count.getOrDefault(loc,0)+1));
        }
        this.number = check().a();
    }

    public double getDouble(String key) throws GameAreaInputException{
        if (!config.contains(key)) throw new GameAreaInputException(this.game,String.format("エリア設定ファイル(%s)の中に%sがないんだが?",this.config.getName(),key));
        String arg = config.getString(key, "null");
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new GameAreaInputException(this.game,String.format("エリア設定ファイル(%s)の中に%sが数値じゃないんだが?",this.config.getName(),key));
        }
//        if (!config.isDouble(key)) throw new GameAreaInputException(this.game,String.format("エリア設定ファイル(%s)の中に%sが数値じゃないんだが?",this.config.getName(),key));
//        return this.config.getDouble(key);
    }

    public Tuple<Integer,Map<Integer,Location>> check() {
        int c = 0;
        for (Location loc:this.locations) for (int i = 0;i <= this.count.getOrDefault(loc,0);i++) map.put(c++,loc);
        return new Tuple<>(c,map);
    }

    public Location getRandom() {
        return this.map.get(this.random.nextInt(this.number));
    }

    public enum Shape {
        BOX,CYLINDER,NULL;

        public static Shape parseShape(String arg) {
            if (arg == null) return NULL;
            if (arg.equalsIgnoreCase("box")) return BOX;
            if (arg.equalsIgnoreCase("cylinder")) return CYLINDER;
            return NULL;
        }
    }

    public enum Type {
        PILE,ADD;

        public static Type parseShape(String arg) {
            if (arg == null) return ADD;
            if (arg.equalsIgnoreCase("pile")) return PILE;
            return ADD;
        }
    }

    private boolean isEmpty(Material material) {
        switch (material) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
            case WATER:
            case GRASS:
            case TALL_GRASS:
            case SEAGRASS:
            case TALL_SEAGRASS:
            case TORCH:
            case WHITE_CARPET:
            case CYAN_CARPET:
            case BLACK_CARPET:
            case BLUE_CARPET:
            case BROWN_CARPET:
            case GRAY_CARPET:
            case GREEN_CARPET:
            case LIGHT_BLUE_CARPET:
            case LIGHT_GRAY_CARPET:
            case LIME_CARPET:
            case MAGENTA_CARPET:
            case ORANGE_CARPET:
            case PINK_CARPET:
            case PURPLE_CARPET:
            case RED_CARPET:
            case YELLOW_CARPET:
            case OAK_SIGN:
            case SPRUCE_SIGN:
            case ACACIA_SIGN:
            case SPRUCE_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case BIRCH_SIGN:
            case BIRCH_WALL_SIGN:
            case DARK_OAK_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_SIGN:
            case JUNGLE_WALL_SIGN:
            case OAK_WALL_SIGN:
            case LADDER:
            case RAIL:
            case ACTIVATOR_RAIL:
            case DETECTOR_RAIL:
            case POWERED_RAIL:
                return true;
            default:
                return false;
        }
    }

    private boolean isLand(Material material) {
        return !isEmpty(material) && material != Material.BARRIER && material != Material.LAVA;
    }

    private interface ShapedGameArea {
        List<Location> getSpawnableLocations();
    }

    private class BoxGameArea implements ShapedGameArea {
        private final int minX,minY,minZ,maxX,maxY,maxZ;

        public BoxGameArea(int minX,int minY,int minZ,int maxX,int maxY,int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMaxZ() {
            return maxZ;
        }

        public int getMinX() {
            return minX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMinZ() {
            return minZ;
        }

        @Override
        public List<Location> getSpawnableLocations() {
            List<Location> locs = new ArrayList<>();
            for (int x = minX;x <= maxX;x++) for (int y = minY;y <= maxY;y++) for (int z = minZ;z <= maxZ;z++) {
                Location base = new Location(world,x,y,z);
                Location land = new Location(world,x,y-1,z);
                Location head = new Location(world,x,y+1,z);
                if (isLand(land.getBlock().getType()) && isEmpty(base.getBlock().getType()) && isEmpty(head.getBlock().getType())) {
                    locs.add(base);
                }
            }
            return locs;
        }
    }

    private class CylinderGameArea implements ShapedGameArea {
        private final int radius,height,centerX,centerY,centerZ;

        public CylinderGameArea(int centerX,int centerY,int centerZ,int radius,int height) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.radius = radius;
            this.height = height;
        }

        public int getCenterX() {
            return centerX;
        }

        public int getCenterY() {
            return centerY;
        }

        public int getCenterZ() {
            return centerZ;
        }

        public int getRadius() {
            return radius;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public List<Location> getSpawnableLocations() {
            List<Location> locs = new ArrayList<>();
            for (int x = -radius;x <= radius;x++) for (int y = 0;y <= height;y++) for (int z = -radius;z <= radius;z++) {
                if (!in(x,z,radius)) continue;
                Location base = new Location(world,x + centerX,y + centerY,z + centerZ);
                Location land = new Location(world,x + centerX,y-1 + centerY,z + centerZ);
                Location head = new Location(world,x + centerX,y+1 + centerY,z + centerZ);
                if (isLand(land.getBlock().getType()) && isEmpty(base.getBlock().getType()) && isEmpty(head.getBlock().getType())) {
                    locs.add(base);
                }
            }
            return locs;
        }

        private boolean in(float x,float z,float rad) {
            return x * x + z * z <= rad * rad;
        }
    }
}