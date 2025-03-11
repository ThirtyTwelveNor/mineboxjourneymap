package net.thirtytwelve;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.thirtytwelve.util.GeoJsonUtils;

/**
 * Utility class for the MineBox JourneyMap integration.
 * Provides convenience methods and dev actions.
 */
public class MineBoxJourneyMapUtil {
    /**
     * Shows a toast notification in the Minecraft UI.
     * This handles both client and server environments safely.
     *
     * @param title The title of the toast notification
     * @param message The content message of the toast notification
     */
    public static void showToast(String title, String message) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.execute(() -> SystemToast.add(
                        client.getToastManager(),
                        SystemToast.Type.WORLD_BACKUP,
                        Text.literal(title),
                        Text.literal(message)
                ));
            }
        } catch (Exception e) {
            // Fail gracefully if we're in a server environment
            System.err.println("Could not show toast: " + e.getMessage());
        }
    }

    /**
     * Development action 1: Download GeoJSON data.
     * Initiates the background download of all GeoJSON files.
     */
    public static void devAction1() {
        System.err.println("1");
        GeoJsonUtils.downloadAllGeoJson();
    }

    /**
     * Development action 2: Parse GeoJSON to waypoints.
     * Initiates the background parsing of GeoJSON files to waypoint format.
     */
    public static void devAction2() {
        System.err.println("2");
        GeoJsonUtils.parseToWaypoints();
    }

    /**
     * Development action 3: Process waypoints and add them to JourneyMap.
     * Processes the waypoint file and creates actual waypoints in JourneyMap.
     */
    public static void devAction3() {
        System.err.println("3");
        if (plugin != null) {
            GeoJsonUtils.processWaypoints();
            int count = 0;
            for (GeoJsonUtils.ParsedWaypoint waypoint : GeoJsonUtils.PARSED_WAYPOINTS) {
                plugin.makeWaypoint(waypoint.name(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z());
                count++;
            }

            // Show toast for the waypoint creation
            showToast("Waypoints Created", "Created " + count + " waypoints in JourneyMap");
        }
    }

    /**
     * Development action 4: Fix duplicate waypoints.
     * Organizes duplicate waypoints into groups in JourneyMap.
     */
    public static void devAction4() {
        System.err.println("4");
        if (plugin != null) {
            int count = plugin.fixWaypoints();

            // Show toast for the waypoint fixing
            showToast("Waypoints Fixed", "Fixed " + count + " waypoints in JourneyMap");
        }
    }

    /** Reference to the JourneyMap plugin instance */
    private static MineBoxJourneyMapPlugin plugin;

    /**
     * Sets the plugin reference.
     * Called during plugin initialization.
     *
     * @param thePlugin The plugin instance
     */
    public static void setPlugin(MineBoxJourneyMapPlugin thePlugin) {
        plugin = thePlugin;
    }
}