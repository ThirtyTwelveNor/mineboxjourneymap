package net.thirtytwelve.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
                new MapConfig("home_island", "minecraft:youruuid/main"),
                new MapConfig("spawn", "minecraft:overworld"),
                new MapConfig("kokoko", "minecraft:island_tropical"),
                new MapConfig("quadra_plains", "minecraft:island_plain"),
                new MapConfig("bamboo_peak", "minecraft:island_bamboo"),
                new MapConfig("frostbite_fortress", "minecraft:island_snow"),
                new MapConfig("sandwhisper_dunes", "minecraft:island_desert")
        );

        config.categories = Arrays.asList("tree", "flower", "ore", "monster", "");

        config.translations = new LinkedHashMap<>();

        // Plants
        config.translations.put("belladonna", "☘belladonna");
        config.translations.put("chamomille", "☘chamomille");
        config.translations.put("clover", "☘clover");
        config.translations.put("echinacea", "☘echinacea");
        config.translations.put("five_leaf_clover", "☘five leaf clover");
        config.translations.put("foxglove", "☘foxglove");
        config.translations.put("ginger_root", "☘ginger root");
        config.translations.put("hemlock", "☘hemlock");
        config.translations.put("henbane", "☘henbane");
        config.translations.put("lily", "☘lily");
        config.translations.put("mandrake", "☘mandrake");
        config.translations.put("mullein", "☘mullein");
        config.translations.put("origami", "☘origami");
        config.translations.put("peppermint", "☘peppermint");
        config.translations.put("snowdrop", "☘snowdrop");
        config.translations.put("st_john_wort", "☘st john's wort");
        config.translations.put("yarrow", "☘yarrow");

// Ores
        config.translations.put("ore_ashstone", "⛏ashstone");
        config.translations.put("ore_bauxite", "⛏bauxite");
        config.translations.put("ore_coal", "⛏coal");
        config.translations.put("ore_cobalt", "⛏cobalt");
        config.translations.put("ore_copper", "⛏copper");
        config.translations.put("ore_diamond", "⛏diamond");
        config.translations.put("ore_dolomite", "⛏dolomite");
        config.translations.put("ore_iron", "⛏iron");
        config.translations.put("ore_lapis_lazuli", "⛏lapis");
        config.translations.put("ore_liquid_diamond", "⛏liquid diamond");
        config.translations.put("ore_manganese", "⛏manganese");
        config.translations.put("ore_opale", "⛏opal");
        config.translations.put("ore_rainbow", "⛏rainbow");
        config.translations.put("ore_raw_obsidian", "⛏raw obsidian");
        config.translations.put("ore_redstone", "⛏redstone");
        config.translations.put("ore_seafoam", "⛏seafoam");
        config.translations.put("ore_silicate", "⛏silicate");
        config.translations.put("ore_silver", "⛏silver");
        config.translations.put("ore_tin", "⛏tin");
        config.translations.put("ore_topaz", "⛏topaz");

// Logs
        config.translations.put("log_banana", "🌲banana");
        config.translations.put("log_chestnut", "🌲chestnut");
        config.translations.put("log_coconut", "🌲coconut");
        config.translations.put("log_dark_coconut", "🌲dark coconut");
        config.translations.put("log_ecalyptus", "🌲eucalyptus");
        config.translations.put("log_elm", "🌲elm");
        config.translations.put("log_hazel", "🌲hazel");
        config.translations.put("log_laughing", "🌲laughing");
        config.translations.put("log_mahogany", "🌲mahogany");
        config.translations.put("log_maple", "🌲maple");
        config.translations.put("log_mystic_horbeam", "🌲mystic hornbeam");
        config.translations.put("log_olive", "🌲olive");
        config.translations.put("log_sacred_coconut", "🌲sacred coconut");
        config.translations.put("log_walnut", "🌲walnut");
        config.translations.put("log_yew", "🌲yew");

// Treasures
        config.translations.put("treasure_common", "🗝common");
        config.translations.put("treasure_uncommon", "🗝uncommon");
        config.translations.put("treasure_rare", "🗝rare");
        config.translations.put("treasure_epic", "🗝epic");
        config.translations.put("treasure_legendary", "🗝legendary");
        config.translations.put("treasure_mythic", "🗝mythic");

// Fish
        config.translations.put("bamboo_peak_fish", "🐠fish");
        config.translations.put("frostbite_fortress_fish", "🐠fish");
        config.translations.put("kokoko_fish", "🐠fish");
        config.translations.put("quadra_plains_fish", "🐠fish");
        config.translations.put("sandwhisper_dunes_fish", "🐠fish");
        config.translations.put("spawn_fish", "🐠fish");

// Bloon
        config.translations.put("bamboo_peak_bloon", "🎈bloon");
        config.translations.put("frostbite_fortress_bloon", "🎈bloon");
        config.translations.put("kokoko_bloon", "🎈bloon");
        config.translations.put("quadra_plains_bloon", "🎈bloon");
        config.translations.put("sandwhisper_dunes_bloon", "🎈bloon");
        config.translations.put("spawn_bloon", "🎈bloon");

// Viewpoints
        config.translations.put("bamboo_peak_viewPoint", "👁viewpoint");
        config.translations.put("frostbite_fortress_viewPoint", "👁viewpoint");
        config.translations.put("kokoko_viewPoint", "👁viewpoint");
        config.translations.put("quadra_plains_viewPoint", "👁viewpoint");
        config.translations.put("sandwhisper_dunes_viewPoint", "👁viewpoint");
        config.translations.put("spawn_viewPoint", "👁viewpoint");

// Coins
        config.translations.put("bamboo_peak_coin", "🪙coin");
        config.translations.put("frostbite_fortress_coin", "🪙coin");
        config.translations.put("kokoko_coin", "🪙coin");
        config.translations.put("quadra_plains_coin", "🪙coin");
        config.translations.put("sandwhisper_dunes_coin", "🪙coin");
        config.translations.put("spawn_coin", "🪙coin");

// NPCs and Monsters
        config.translations.put("bambooboo", "♥bambooboo");
        config.translations.put("bloomboo", "♥bloomboo");
        config.translations.put("cat_goofish", "♥cat goofish");
        config.translations.put("chillolith", "♥chillolith");
        config.translations.put("clown_goofish", "♥clown goofish");
        config.translations.put("coconut_magician", "♥coconut magician");
        config.translations.put("coconut_warrior", "♥coconut warrior");
        config.translations.put("crabician", "♥crabician");
        config.translations.put("craboxer", "♥craboxer");
        config.translations.put("debris_golem", "♥debris golem");
        config.translations.put("hammershark_goofish", "♥hammershark goofish");
        config.translations.put("monkey", "♥monkey");
        config.translations.put("monkey_banana", "♥banana monkey");
        config.translations.put("monkey_barrel", "♥barrel monkey");
        config.translations.put("monkey_ghost", "♥ghost monkey");
        config.translations.put("monkey_mage", "♥mage monkey");
        config.translations.put("moskitoko", "♥moskitoko");
        config.translations.put("old_pirate_farmer", "♥old pirate farmer");
        config.translations.put("orange_starfish", "♥orange starfish");
        config.translations.put("pandaboo_warrior", "♥pandaboo warrior");
        config.translations.put("pandaboo_wizard", "♥pandaboo wizard");
        config.translations.put("pirate_farmer", "♥pirate farmer");
        config.translations.put("scavenger_vulture", "♥scavenger vulture");
        config.translations.put("scarecrow", "♥scarecrow");
        config.translations.put("spawn_npc", "♥npc");
        config.translations.put("spicy_lava_bucket", "♥spicy lava bucket");
        config.translations.put("spidey", "♥spidey");
        config.translations.put("venomous_spidey", "♥venomous spidey");

        return config;
    }

    public void setMaps(List<MapConfig> maps) {
        this.maps = new ArrayList<>(maps);
    }

    public void setCategories(List<String> categories) {
        this.categories = new ArrayList<>(categories);
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = new LinkedHashMap<>(translations);
    }

    public Map<String, String> getTranslations() {
        return new LinkedHashMap<>(translations);
    }

    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = loadConfig();
        }
        return INSTANCE;
    }

    public static void reloadConfig() {
        INSTANCE = null; // Clear the current instance
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

    public static void saveConfig(Config config) {
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