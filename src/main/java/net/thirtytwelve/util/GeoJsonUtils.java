package net.thirtytwelve.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.thirtytwelve.config.Config;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static net.thirtytwelve.MineBoxJourneyMap.MOD_ID;

public class GeoJsonUtils {
    private static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    private static Path getGeoJsonDir() {
        return getConfigDir().resolve("geojson_data");
    }

    private static Path getWaypointsFile() {
        return getConfigDir().resolve("waypoints.txt");
    }

    public static void downloadAllGeoJson() {
        Path downloadDir = getGeoJsonDir();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();

        try {
            Files.createDirectories(downloadDir);
            Config config = Config.getInstance();

            for (Config.MapConfig map : config.getMaps()) {
                for (String markerId : config.getMarkers()) {
                    for (String category : config.getCategories()) {
                        if (tryDownloadGeoJson(httpClient, downloadDir, map.id, markerId, category)) {
                            System.out.println("Downloaded: " + map.id + "/" + markerId);
                            break;  // Found the right category, skip others
                        }
                        //TimeUnit.MILLISECONDS.sleep(50);
                    }
                }
            }
            System.out.println("Download complete!");
        } catch (Exception e) {
            System.err.println("Error downloading files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean tryDownloadGeoJson(HttpClient client, Path outputDir,
                                              String mapName, String marker, String category) {
        try {
            String categoryPath = category.isEmpty() ? "" : category + "/";
            String url = String.format("https://mineboxmaps.com/assets/geo/%s/%s%s.geojson",
                    mapName, categoryPath, marker);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Path outputPath = outputDir.resolve(marker + ".geojson");
                //Files.writeString(outputPath, response.body(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                if (!Files.exists(outputPath)) {
                    // First time seeing this marker - just write the downloaded content directly
                    Files.writeString(outputPath, response.body());
                } else {
                    // Append to existing file
                    String existingContent = Files.readString(outputPath);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonObject existingRoot = gson.fromJson(existingContent, JsonObject.class);
                    JsonArray existingFeatures = existingRoot.getAsJsonArray("features");

                    // Parse new content and append its features
                    JsonObject newRoot = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray newFeatures = newRoot.getAsJsonArray("features");
                    newFeatures.forEach(existingFeatures::add);

                    // Write back the combined content
                    Files.writeString(outputPath, gson.toJson(existingRoot));
                }

                System.out.println("Successfully fetched: " + url);
                return true;
            }
        } catch (Exception e) {
            // Silently ignore 404s and connection errors
        }
        return false;
    }

    public static void parseToWaypoints() {
        Path inputDir = getGeoJsonDir();
        Path outputFile = getWaypointsFile();

        try {
            Files.createDirectories(outputFile.getParent());
            List<String> waypoints = new ArrayList<>();

            try (Stream<Path> paths = Files.walk(inputDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".geojson"))
                        .forEach(path -> processGeoJsonFile(path, waypoints));
            }

            Files.write(outputFile, waypoints);
            System.out.println("Created waypoints file with " + waypoints.size() + " entries at: " + outputFile);
        } catch (Exception e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void processGeoJsonFile(Path filePath, List<String> waypoints) {
        try {
            String content = Files.readString(filePath);
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(content, JsonObject.class);

            if (!root.has("features")) return;

            JsonArray features = root.getAsJsonArray("features");
            if (features.isEmpty()) return;

            String markerId = filePath.getFileName().toString().replace(".geojson", "");
            Config config = Config.getInstance();
            String baseId = config.cleanMapPrefix(markerId);
            String displayName = config.getTranslation(baseId);

            // Process each feature individually to respect its map property
            for (var feature : features) {
                JsonObject featureObj = feature.getAsJsonObject();
                JsonObject geometry = featureObj.getAsJsonObject("geometry");

                if (!geometry.get("type").getAsString().equals("Point")) continue;


                String mapName = featureObj.has("properties") &&
                        featureObj.getAsJsonObject("properties").has("map") ?
                        featureObj.getAsJsonObject("properties").get("map").getAsString() : "";

                // Get the correct dimension for this feature's map
                Config.MapConfig mapConfig = config.getMaps().stream()
                        .filter(map -> map.id.equals(mapName))
                        .findFirst()
                        .orElse(null);

                if (mapConfig == null) {
                    System.err.println("Unknown map: " + mapName);
                    continue;
                }

                JsonArray coords = geometry.getAsJsonArray("coordinates");
                if (coords.size() != 3) continue;

                String waypoint = String.format("/wp create %s %s %s %s %s aqua @p true",
                        displayName,
                        mapConfig.mcDimension,
                        coords.get(0).getAsString(),
                        coords.get(1).getAsString(),
                        coords.get(2).getAsString()
                );
                waypoints.add(waypoint);
            }
        } catch (Exception e) {
            System.err.println("Error processing file " + filePath + ": " + e.getMessage());
        }
    }
}