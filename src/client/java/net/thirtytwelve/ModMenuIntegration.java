package net.thirtytwelve;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.thirtytwelve.config.Config;

import java.util.*;

public class ModMenuIntegration implements ModMenuApi {
    // Track ID changes
    private final Map<String, String> idChanges = new HashMap<>();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("minebox.config.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        Config config = Config.getInstance();

        // Maps Category
        ConfigCategory mapsCategory = builder.getOrCreateCategory(Text.translatable("minebox.config.category.maps"));
        List<Config.MapConfig> maps = new ArrayList<>(config.getMaps());

        for (int i = 0; i < maps.size(); i++) {
            final int index = i;
            Config.MapConfig map = maps.get(i);

            mapsCategory.addEntry(entryBuilder.startTextDescription(
                    Text.literal("Map " + (i + 1))
            ).build());

            List<String> mapId = new ArrayList<>(Collections.singletonList(map.id));
            mapsCategory.addEntry(entryBuilder.startStrList(
                            Text.translatable("minebox.config.map.id", i + 1),
                            mapId)
                    .setDefaultValue(Collections.singletonList(map.id))
                    .setExpanded(true)
                    .setSaveConsumer(newValue -> maps.get(index).id = newValue.isEmpty() ? "" : newValue.getFirst())
                    .build());

            List<String> dimension = new ArrayList<>(Collections.singletonList(map.mcDimension));
            mapsCategory.addEntry(entryBuilder.startStrList(
                            Text.translatable("minebox.config.map.dimension", i + 1),
                            dimension)
                    .setDefaultValue(Collections.singletonList(map.mcDimension))
                    .setExpanded(true)
                    .setSaveConsumer(newValue -> maps.get(index).mcDimension = newValue.isEmpty() ? "" : newValue.getFirst())
                    .build());

            mapsCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build());
        }

        // Categories List
        ConfigCategory categoriesCategory = builder.getOrCreateCategory(Text.translatable("minebox.config.category.categories"));
        List<String> categories = new ArrayList<>(config.getCategories());

        categoriesCategory.addEntry(entryBuilder.startStrList(
                        Text.translatable("minebox.config.categories"),
                        categories)
                .setDefaultValue(config.getCategories())
                .setExpanded(true)
                .setTooltip(Text.literal("Use a single space to represent an empty line"))
                .setSaveConsumer(newCategories -> {
                    categories.clear();
                    categories.addAll(newCategories);
                })
                .build());

        // Translations Category
        ConfigCategory translationsCategory = builder.getOrCreateCategory(Text.translatable("minebox.config.category.translations"));
        Map<String, String> translations = new LinkedHashMap<>(config.getTranslations());

        // Clear previous ID changes when creating new screen
        idChanges.clear();

        for (String marker : config.getMarkers()) {
            List<String> markerId = new ArrayList<>(Collections.singletonList(marker));
            translationsCategory.addEntry(entryBuilder.startStrList(
                            Text.literal("ID: " + marker),
                            markerId)
                    .setDefaultValue(Collections.singletonList(marker))
                    .setExpanded(true)
                    .setSaveConsumer(newValue -> {
                        String newId = newValue.isEmpty() ? "" : newValue.getFirst();
                        if (!marker.equals(newId)) {
                            idChanges.put(marker, newId);
                        }
                    })
                    .build());

            List<String> value = new ArrayList<>(Collections.singletonList(translations.getOrDefault(marker, "")));
            translationsCategory.addEntry(entryBuilder.startStrList(
                            Text.literal("Value for: " + marker),
                            value)
                    .setDefaultValue(Collections.singletonList(translations.getOrDefault(marker, "")))
                    .setExpanded(true)
                    .setSaveConsumer(newValue -> {
                        String id = idChanges.getOrDefault(marker, marker);
                        translations.put(id, newValue.isEmpty() ? "" : newValue.getFirst());
                    })
                    .build());

            translationsCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build());
        }

        // Dev Category
        ConfigCategory devCategory = builder.getOrCreateCategory(Text.literal("Dev"));

        // Button 1
        devCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build());
        devCategory.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Dev Action 1"),
                        false)
                .setSaveConsumer(value -> {
                    if (value) MineBoxJourneyMapUtil.devAction1();
                })
                .build());

        // Button 2
        devCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build());
        devCategory.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Dev Action 2"),
                        false)
                .setSaveConsumer(value -> {
                    if (value) MineBoxJourneyMapUtil.devAction2();
                })
                .build());

        // Button 3
        devCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build());
        devCategory.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Dev Action 3"),
                        false)
                .setSaveConsumer(value -> {
                    if (value) MineBoxJourneyMapUtil.devAction3();
                })
                .build());

        // Button 4
        devCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build());
        devCategory.addEntry(entryBuilder.startBooleanToggle(
                        Text.literal("Dev Action 4"),
                        false)
                .setSaveConsumer(value -> {
                    if (value) MineBoxJourneyMapUtil.devAction4();
                })
                .build());

        // Save Handler
        builder.setSavingRunnable(() -> {
            try {
                // Process ID changes and update translations
                Map<String, String> updatedTranslations = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : translations.entrySet()) {
                    String oldId = entry.getKey();
                    String newId = idChanges.getOrDefault(oldId, oldId);
                    updatedTranslations.put(newId, entry.getValue());
                }

                // Save with the processed translations
                saveConfig(maps, categories, updatedTranslations);
                Config.reloadConfig();
            } catch (Exception e) {
                System.err.println("Failed to save config: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return builder.build();
    }


    private void saveConfig(List<Config.MapConfig> maps, List<String> categories, Map<String, String> translations) {
        Config newConfig = new Config();
        newConfig.setMaps(maps);
        newConfig.setCategories(categories);
        newConfig.setTranslations(translations);
        Config.saveConfig(newConfig);
    }
}