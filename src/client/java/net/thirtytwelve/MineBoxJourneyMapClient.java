package net.thirtytwelve;

import net.fabricmc.api.ClientModInitializer;
import net.thirtytwelve.config.Config;

public class MineBoxJourneyMapClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		Config.getInstance();
	}
}