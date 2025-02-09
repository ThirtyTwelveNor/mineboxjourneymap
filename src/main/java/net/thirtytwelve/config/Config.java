package net.thirtytwelve.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.thirtytwelve.MineBoxJourneyMap.MOD_ID;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config INSTANCE;

    private List<MapConfig> maps;
    private Map<String, String> translations;
    private List<String> categories;

    public static class MapConfig {
        public String id;  // Changed to public
        public String mcDimension;  // Changed to public for consistency

        public MapConfig(String id, String mcDimension) {
            this.id = id;
            this.mcDimension = mcDimension;
        }
    }

    private static Config createDefaultConfig() {
        Config config = new Config();

        config.maps = Arrays.asList(
                new MapConfig("spawn", "minecraft:overworld"),
                new MapConfig("kokoko", "minecraft:island_tropical"),
                new MapConfig("quadra_plains", "minecraft:island_plain")
        );

        config.categories = Arrays.asList("tree", "flower", "ore", "monster", "");

        config.translations = new LinkedHashMap<>();
// Bloons
        config.translations.put("kokoko_bloon", "bloon");
        config.translations.put("quadra_plains_bloon", "bloon");
        config.translations.put("spawn_bloon", "bloon");

// Coins
        config.translations.put("kokoko_coin", "coin");
        config.translations.put("quadra_plains_coin", "coin");
        config.translations.put("spawn_coin", "coin");
// ViewPoints
        config.translations.put("kokoko_viewPoint", "viewPoint");
        config.translations.put("quadra_plains_viewPoint", "viewPoint");
        config.translations.put("spawn_viewPoint", "viewPoint");

// Fish
        config.translations.put("blue_starfish", "blue_starfish");
        config.translations.put("cat_goofish", "cat_goofish");
        config.translations.put("clown_goofish", "clown_goofish");
        config.translations.put("hammershark_goofish", "hammershark_goofish");
        config.translations.put("orange_starfish", "orange_starfish");

        config.translations.put("kokoko_fish", "fish");
        config.translations.put("quadra_plains_fish", "fish");
        config.translations.put("spawn_fish", "fish");

// Logs
        config.translations.put("log_banana", "banana");
        config.translations.put("log_chestnut", "chestnut");
        config.translations.put("log_coconut", "coconut");
        config.translations.put("log_dark_coconut", "dark_coconut");
        config.translations.put("log_mahogany", "mahogany");
        config.translations.put("log_mystic_horbeam", "mystic_horbeam");
        config.translations.put("log_sacred_coconut", "sacred_coconut");
        config.translations.put("log_walnut", "walnut");

// Monkeys
        config.translations.put("monkey", "monkey");
        config.translations.put("monkey_banana", "monkey_banana");
        config.translations.put("monkey_barrel", "monkey_barrel");
        config.translations.put("monkey_ghost", "monkey_ghost");
        config.translations.put("monkey_mage", "monkey_mage");

// Ores
        config.translations.put("ore_ashstone", "ashstone");
        config.translations.put("ore_bauxite", "bauxite");
        config.translations.put("ore_coal", "coal");
        config.translations.put("ore_cobalt", "cobalt");
        config.translations.put("ore_copper", "copper");
        config.translations.put("ore_dolomite", "dolomite");
        config.translations.put("ore_iron", "iron");
        config.translations.put("ore_rainbow", "rainbow");
        config.translations.put("ore_raw_obsidian", "raw_obsidian");
        config.translations.put("ore_silver", "silver");

// Others
        config.translations.put("coconut_magician", "coconut_magician");
        config.translations.put("coconut_warrior", "coconut_warrior");
        config.translations.put("crabician", "crabician");
        config.translations.put("craboxer", "craboxer");
        config.translations.put("debris_golem", "debris_golem");
        config.translations.put("moskitoko", "moskitoko");
        config.translations.put("scarecrow", "scarecrow");
        config.translations.put("scavenger_vulture", "scavenger_vulture");
        config.translations.put("spawn_npc", "npc");
        config.translations.put("spicy_lava_bucket", "spicy_lava_bucket");

// Pirates
        config.translations.put("old_pirate_farmer", "old_pirate_farmer");
        config.translations.put("pirate_farmer", "pirate_farmer");

// Plants
        config.translations.put("belladonna", "belladonna");
        config.translations.put("chamomille", "chamomille");
        config.translations.put("clover", "clover");
        config.translations.put("echinacea", "echinacea");
        config.translations.put("hemlock", "hemlock");
        config.translations.put("henbane", "henbane");
        config.translations.put("lily", "lily");
        config.translations.put("mandrake", "mandrake");
        config.translations.put("peppermint", "peppermint");

// Spiders
        config.translations.put("spidey", "spidey");
        config.translations.put("venomous_spidey", "venomous_spidey");
        return config;
    }

    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = loadConfig();
        }
        return INSTANCE;
    }

    private static Config loadConfig() {
        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            Config defaultConfig = createDefaultConfig();
            saveConfig(defaultConfig);
            return defaultConfig;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            return GSON.fromJson(reader, Config.class);
        } catch (Exception e) {
            System.err.println("Error loading config, using defaults: " + e.getMessage());
            return createDefaultConfig();
        }
    }

    private static void saveConfig(Config config) {
        try {
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir()
                .resolve(MOD_ID)
                .resolve("geojson_config.json");
    }

    public List<MapConfig> getMaps() {
        return maps;
    }

    public List<String> getCategories() {
        return categories;
    }

    public Set<String> getMarkers() {
        return translations.keySet();
    }

    public String getTranslation(String id) {
        return translations.getOrDefault(id, id);
    }

    public String cleanMapPrefix(String id) {
        for (MapConfig map : maps) {
            String prefix = map.id + "_";
            if (id.startsWith(prefix)) {
                return id.substring(prefix.length());
            }
        }
        return id;
    }
}