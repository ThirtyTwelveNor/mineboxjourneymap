package net.thirtytwelve;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.thirtytwelve.MineBoxJourneyMap.MOD_ID;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class MineBoxJourneyMapPlugin implements IClientPlugin {
    private IClientAPI jmAPI;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void initialize(IClientAPI api) {
        this.jmAPI = api;
    }

    public void fixWaypoints() {
        // Get all waypoints
        List<? extends Waypoint> allWaypoints = jmAPI.getAllWaypoints();

        // Track created groups by name
        Map<String, WaypointGroup> groupsByName = new HashMap<>();

        for (Waypoint waypoint : allWaypoints) {
            String waypointName = waypoint.getName();

            // Count how many waypoints exist with this name
            long matchingCount = allWaypoints.stream()
                    .filter(w -> w.getName().equals(waypointName))
                    .count();

            // Only process if there are duplicates
            if (matchingCount > 1) {
                // Try to get existing group first by name
                WaypointGroup group = groupsByName.get(waypointName);
                if (group == null) {
                    group = jmAPI.getWaypointGroupByName(MOD_ID, waypointName);

                    if (group == null) {
                        // Create new group
                        group = WaypointFactory.createWaypointGroup(MOD_ID, waypointName);
                        jmAPI.addWaypointGroup(group);
                    }
                    groupsByName.put(waypointName, group);
                }

                // Check if waypoint needs to be added to this group
                String groupId = group.getGuid();
                if (!waypoint.getGroupId().equals(groupId)) {
                    group.addWaypoint(waypoint);
                }
            }
        }
    }

    public void makeWaypoint(String name, String dimension, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);

        Waypoint waypoint = WaypointFactory.createClientWaypoint(MOD_ID, pos, name, dimension, true);

        // Set default colors (optional - you can modify these)
        waypoint.setColor(0xFF0000); // Red color
        waypoint.setEnabled(true);

        // Add the waypoint to JourneyMap
        jmAPI.addWaypoint(MOD_ID, waypoint);
    }
}