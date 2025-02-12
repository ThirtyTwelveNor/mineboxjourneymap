package net.thirtytwelve;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointGroup;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.util.math.BlockPos;

import java.util.*;

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
        MineBoxJourneyMapUtil.setPlugin(this);
    }
    public void fixWaypoints() {
        // Get all waypoints from our mod
        List<? extends Waypoint> myWaypoints = jmAPI.getWaypoints(MOD_ID);

        // Track created groups by name
        Map<String, WaypointGroup> groupsByName = new HashMap<>();

        // First, collect all waypoint names and their counts
        Map<String, List<Waypoint>> waypointsByName = new HashMap<>();

        for (Waypoint waypoint : myWaypoints) {
            // Skip if waypoint is already in one of our groups
            if (jmAPI.getWaypointGroup(waypoint.getGroupId()).getModId().equals(MOD_ID)) {
                continue;
            }

            String waypointName = waypoint.getName();
            waypointsByName.computeIfAbsent(waypointName, k -> new ArrayList<>()).add(waypoint);
        }

        // Process waypoints that have duplicates
        for (Map.Entry<String, List<Waypoint>> entry : waypointsByName.entrySet()) {
            String waypointName = entry.getKey();
            List<Waypoint> waypoints = entry.getValue();

            if (waypoints.size() > 1) {
                WaypointGroup group = groupsByName.get(waypointName);
                if (group == null) {
                    group = WaypointFactory.createWaypointGroup(MOD_ID, waypointName);
                    group.setEnabled(false);
                    group.setColorOverride(true);
                    jmAPI.addWaypointGroup(group);
                    groupsByName.put(waypointName, group);
                }

                // Add all waypoints to the group
                for (Waypoint waypoint : waypoints) {
                    group.addWaypoint(waypoint);
                }
            }
        }
    }

    public void makeWaypoint(String name, String dimension, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);

        Waypoint waypoint = WaypointFactory.createClientWaypoint(MOD_ID, pos, name, dimension, true);

        // Set default colors (optional - you can modify these)
        waypoint.setColor(0x00FFFF); // Light blue color
        waypoint.setEnabled(true);

        // Add the waypoint to JourneyMap
        jmAPI.addWaypoint(MOD_ID, waypoint);
    }
}



























