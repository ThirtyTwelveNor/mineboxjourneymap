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
        MineBoxJourneyMapUtil.setPlugin(this);
    }

    public void fixWaypoints() {
        // Get all waypoints from our mod
        List<? extends Waypoint> myWaypoints = jmAPI.getWaypoints(MOD_ID);

        // Track created groups by name
        Map<String, WaypointGroup> groupsByName = new HashMap<>();

        for (Waypoint waypoint : myWaypoints) {
            // Skip if waypoint is already in one of our groups
            if (jmAPI.getWaypointGroup(waypoint.getGroupId()).getModId().equals(MOD_ID)) {
                continue;
            }

            String waypointName = waypoint.getName();

            // Count matching waypoints that are:
            // 1. From our mod (redundant since myWaypoints is already filtered)
            // 2. Still in default group (not in one of our groups)
            long matchingCount = myWaypoints.stream()
                    .filter(w -> !jmAPI.getWaypointGroup(w.getGroupId()).getModId().equals(MOD_ID))
                    .filter(w -> w.getName().equals(waypointName))
                    .count();

            if (matchingCount > 1) {
                WaypointGroup group = groupsByName.get(waypointName);
                if (group == null) {
                    group = WaypointFactory.createWaypointGroup(MOD_ID, waypointName);
                    jmAPI.addWaypointGroup(group);
                    groupsByName.put(waypointName, group);
                }

                group.addWaypoint(waypoint);
                group.setColorOverride(true);
                group.setEnabled(false);
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



























