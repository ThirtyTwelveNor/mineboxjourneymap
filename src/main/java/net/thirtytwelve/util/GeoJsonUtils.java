package net.thirtytwelve.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.thirtytwelve.config.Config;
import net.fabricmc.loader.api.FabricLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.thirtytwelve.MineBoxJourneyMap.MOD_ID;

/**
 * Utility class for downloading and processing GeoJSON data into waypoints.
 * Handles background processing of map data for JourneyMap integration.
 */
public class GeoJsonUtils {
    /** Thread pool for background operations */
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    /** List of parsed waypoints available for use */
    public static final List<ParsedWaypoint> PARSED_WAYPOINTS = new ArrayList<>();

    /**
     * Gets the mod's config directory.
     *
     * @return Path to the mod's config directory
     */
    private static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    /**
     * Gets the directory where GeoJSON files are stored.
     *
     * @return Path to the GeoJSON directory
     */
    private static Path getGeoJsonDir() {
        return getConfigDir().resolve("geojson_data");
    }

    /**
     * Gets the path to the generated waypoints file.
     *
     * @return Path to the waypoints file
     */
    private static Path getWaypointsFile() {
        return getConfigDir().resolve("waypoints.txt");
    }

    /**
     * Downloads all GeoJSON files in a background thread.
     * Fetches marker data from the MineBox Maps service.
     */
    public static void downloadAllGeoJson() {
        Thread downloadThread = new Thread(() -> {
            try {
                Path downloadDir = getGeoJsonDir();
                Files.createDirectories(downloadDir);
                Config config = Config.getInstance();

                HttpClient httpClient = HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(10))
                        .build();

                List<Thread> downloadThreads = new ArrayList<>();
                AtomicInteger totalDownloaded = new AtomicInteger(0);

                // Print how many downloads we're attempting
                int totalMarkers = config.getMaps().size() * config.getMarkers().size();
                System.out.println("Attempting to download " + totalMarkers + " markers across " +
                        config.getMaps().size() + " maps...");

                for (Config.MapConfig map : config.getMaps()) {
                    for (String markerId : config.getMarkers()) {
                        Thread markerThread = new Thread(() -> {
                            for (String category : config.getCategories()) {
                                // Small delay between requests to prevent rate limiting
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }

                                if (tryDownloadGeoJson(httpClient, downloadDir, map.id, markerId, category)) {
                                    System.out.println("Downloaded: " + map.id + "/" + markerId +
                                            " - Total: " + totalDownloaded.incrementAndGet());
                                    break;  // Found the right category, skip others
                                }
                            }
                        });
                        downloadThreads.add(markerThread);
                        markerThread.start();

                        // Small delay between starting threads to prevent hammering the server
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                // Wait for all download threads to complete
                for (Thread thread : downloadThreads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                String message = "Downloaded " + totalDownloaded.get() + " files";
                System.out.println("Download complete! " + message);
                showToast("Download Complete", message);
            } catch (Exception e) {
                System.err.println("Error downloading files: " + e.getMessage());
                e.printStackTrace();
            }
        });
        downloadThread.setName("GeoJson-Downloader");
        downloadThread.start();
    }

    /**
     * Attempts to download a specific GeoJSON file.
     *
     * @param client The HTTP client to use
     * @param outputDir The directory to save the file to
     * @param mapName The map ID
     * @param marker The marker ID
     * @param category The category path
     * @return true if download was successful, false otherwise
     */
    private static boolean tryDownloadGeoJson(HttpClient client, Path outputDir,
                                              String mapName, String marker, String category) {
        try {
            String categoryPath = category.isEmpty() ? "" : category + "/";
            String url = String.format("https://mineboxmaps.com/assets/geo/%s/%s%s.geojson",
                    mapName, categoryPath, marker);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "MinecraftMod/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Path outputPath = outputDir.resolve(marker + ".geojson");

                synchronized (GeoJsonUtils.class) {
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
                }

                return true;
            }
        } catch (Exception e) {
            // Log the error without breaking the flow
            System.err.println("Error downloading " + mapName + "/" + marker + "/" + category + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Parses GeoJSON files to waypoints file in a background thread.
     * Processes downloaded GeoJSON into a format usable by JourneyMap.
     */
    public static void parseToWaypoints() {
        Thread parseThread = new Thread(() -> {
            try {
                Path inputDir = getGeoJsonDir();
                Path outputFile = getWaypointsFile();

                Files.createDirectories(outputFile.getParent());
                List<String> waypoints = new ArrayList<>();
                List<Path> filePaths = new ArrayList<>();

                // First collect all file paths
                try (Stream<Path> paths = Files.walk(inputDir)) {
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".geojson"))
                            .forEach(filePaths::add);
                }

                System.out.println("Found " + filePaths.size() + " GeoJson files to process");

                // Process files in parallel
                List<Thread> processThreads = new ArrayList<>();
                List<List<String>> waypointBatches = new ArrayList<>();

                for (Path path : filePaths) {
                    List<String> batch = new ArrayList<>();
                    waypointBatches.add(batch);

                    Thread thread = new Thread(() -> processGeoJsonFile(path, batch));
                    processThreads.add(thread);
                    thread.start();
                }

                // Wait for all processing to complete
                for (Thread thread : processThreads) {
                    thread.join();
                }

                // Combine all batches
                for (List<String> batch : waypointBatches) {
                    synchronized (GeoJsonUtils.class) {
                        waypoints.addAll(batch);
                    }
                }

                Files.write(outputFile, waypoints);

                String message = "Created " + waypoints.size() + " waypoints";
                System.out.println("Created waypoints file with " + waypoints.size() + " entries at: " + outputFile);
                showToast("Waypoints Processed", message);
            } catch (Exception e) {
                System.err.println("Error processing files: " + e.getMessage());
                e.printStackTrace();
            }
        });
        parseThread.setName("GeoJson-Parser");
        parseThread.start();
    }

    /**
     * Processes a single GeoJSON file into waypoint commands.
     *
     * @param filePath Path to the GeoJSON file
     * @param waypoints List to add the generated waypoint commands to
     */
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

                if (geometry.get("type").getAsString().equals("Polygon")) {
                    // Perhaps handle this in the future for mob spawning zones
                    continue;
                }
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

    /**
     * Record representing a parsed waypoint with all necessary data.
     */
    public record ParsedWaypoint(String name, String dimension, int x, int y, int z) {
    }

    /**
     * Process waypoints on the main thread for UI compatibility.
     * This method runs synchronously to load waypoints from the file.
     */
    public static void processWaypoints() {
        Path waypointsFile = getWaypointsFile();
        PARSED_WAYPOINTS.clear(); // Clear existing waypoints before loading

        try {
            if (!Files.exists(waypointsFile)) {
                System.err.println("Waypoints file not found at: " + waypointsFile);
                return;
            }

            List<String> lines = Files.readAllLines(waypointsFile);
            for (String line : lines) {
                processWaypointLine(line.trim());
            }

            String message = "Loaded " + PARSED_WAYPOINTS.size() + " waypoints";
            System.out.println("Processed " + PARSED_WAYPOINTS.size() + " waypoints");
            showToast("Waypoints Ready", message);
        } catch (Exception e) {
            System.err.println("Error reading waypoint file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Processes a single line from the waypoints file.
     * Parses command format into a structured waypoint object.
     *
     * @param line The waypoint command line to process
     */
    private static void processWaypointLine(String line) {
        if (line.isEmpty()) return;

        String[] parts = line.split(" ");

        // Basic validation - we need at least the command, name, dimension, and coordinates
        if (parts.length < 7) {
            System.err.println("Not enough parts in waypoint line: " + line);
            return;
        }

        try {
            // Find dimension index by looking for the first part that matches dimension format
            int dimensionIndex = -1;
            for (int i = 3; i < parts.length - 3; i++) {
                if (isDimensionFormat(parts[i])) {
                    dimensionIndex = i;
                    break;
                }
            }

            if (dimensionIndex == -1) {
                System.err.println("No valid dimension format found in line: " + line);
                return;
            }

            // Combine all parts between index 2 and dimension index for the name
            StringBuilder nameBuilder = new StringBuilder(parts[2]);
            for (int i = 3; i < dimensionIndex; i++) {
                nameBuilder.append(" ").append(parts[i]);
            }
            String name = nameBuilder.toString();

            String dimension = parts[dimensionIndex];
            int x = Integer.parseInt(parts[dimensionIndex + 1]);
            int y = Integer.parseInt(parts[dimensionIndex + 2]);
            int z = Integer.parseInt(parts[dimensionIndex + 3]);

            ParsedWaypoint waypoint = new ParsedWaypoint(name, dimension, x, y, z);
            PARSED_WAYPOINTS.add(waypoint);
        } catch (NumberFormatException e) {
            System.err.println("Invalid coordinates in line: " + line);
        } catch (Exception e) {
            System.err.println("Error processing line: " + line);
        }
    }

    /**
     * Checks if a string matches the dimension format pattern.
     *
     * @param part The string to check
     * @return true if the string matches the dimension format, false otherwise
     */
    private static boolean isDimensionFormat(String part) {
        // Check if the part matches word:word or word_word:word_word format
        return part.matches("^\\S+:\\S+$");
    }

    /**
     * Shuts down the executor service.
     */
    private static void showToast(String title, String message) {
        try {
            // Use reflection to avoid direct dependency
            Class<?> utilClass = Class.forName("net.thirtytwelve.MineBoxJourneyMapUtil");
            java.lang.reflect.Method showToastMethod = utilClass.getMethod("showToast", String.class, String.class);
            showToastMethod.invoke(null, title, message);
        } catch (Exception e) {
            // Fail silently if the toast can't be shown
            System.err.println("Could not show toast: " + e.getMessage());
        }
    }
    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}