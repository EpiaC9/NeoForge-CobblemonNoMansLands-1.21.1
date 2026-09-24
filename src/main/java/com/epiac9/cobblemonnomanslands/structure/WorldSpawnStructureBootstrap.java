package com.epiac9.cobblemonnomanslands.structure;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = CobblemonNoMansLands.MODID)
public class WorldSpawnStructureBootstrap {
    private WorldSpawnStructureBootstrap() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        WorldSpawnStructureService.getConnectionRegistry().clear();
        var level = event.getServer().overworld();
        WorldSpawnStructureService.generateAtWorldSpawn(level);
        //place the structure at world spawn
    }
}
