package com.epiac9.cobblemonnomanslands;

import com.epiac9.cobblemonnomanslands.client.ExplorationSelectionClientState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@Mod(value = CobblemonNoMansLands.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CobblemonNoMansLands.MODID, value = Dist.CLIENT)
public class CobblemonNoMansLandsClient {
	@SubscribeEvent
	public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		ExplorationSelectionClientState.clear();
	}
}
