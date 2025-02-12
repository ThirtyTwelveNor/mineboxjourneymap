package net.thirtytwelve;


import net.thirtytwelve.util.GeoJsonUtils;


public class MineBoxJourneyMapUtil {
    public static void devAction1() {
        // Implement your dev action 1 here
        System.err.println("1");

        GeoJsonUtils.downloadAllGeoJson();
    }

    public static void devAction2() {
        // Implement your dev action 2 here
        System.err.println("2");

        GeoJsonUtils.parseToWaypoints();
    }

    public static void devAction3() {
        // Implement your dev action 3 here
        System.err.println("3");
        if(plugin != null) {
            GeoJsonUtils.processWaypoints();
            for (GeoJsonUtils.ParsedWaypoint waypoint : GeoJsonUtils.PARSED_WAYPOINTS) {
                plugin.makeWaypoint(waypoint.name(), waypoint.dimension(), waypoint.x(), waypoint.y(), waypoint.z());
            }
        }
    }

    public static void devAction4() {
        // Implement your dev action 4 here
        System.err.println("4");
        if(plugin != null) {
            plugin.fixWaypoints();
        }
    }
    private static MineBoxJourneyMapPlugin plugin;
    public  static void setPlugin(MineBoxJourneyMapPlugin thePlugin) {
        plugin = thePlugin;
    }
}
