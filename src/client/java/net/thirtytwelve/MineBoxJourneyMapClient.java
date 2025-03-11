package net.thirtytwelve;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.thirtytwelve.config.Config;
import net.thirtytwelve.util.GeoJsonUtils;

public class MineBoxJourneyMapClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		Config.getInstance();

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> GeoJsonUtils.shutdown());
	}
}