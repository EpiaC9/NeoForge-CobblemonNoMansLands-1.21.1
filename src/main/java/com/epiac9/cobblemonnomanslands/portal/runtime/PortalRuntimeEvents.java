package com.epiac9.cobblemonnomanslands.portal.runtime;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.dungeon.DungeonDimesionKeys;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CobblemonNoMansLands.MODID)
public final class PortalRuntimeEvents {
    private static PortalSystemRuntime runtime;

    private PortalRuntimeEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        runtime = new PortalSystemRuntime();
    } //access method

    @SubscribeEvent
    public static void onServerStopped(ServerStoppingEvent event) {
        runtime = null;
    } //event handler

    public static PortalSystemRuntime getRuntime() {
        if (runtime == null) {
            throw new  IllegalStateException("Portal runtime not initialized");
        }
        return runtime;
    } //runtime state

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (runtime == null) {
            return;
        }

        ServerLevel dungeonLevel = event.getServer().getLevel(DungeonDimesionKeys.DUNGEON);
        if (dungeonLevel == null) {
            return;
        }

        runtime.getGenerationQueue().tick(dungeonLevel);
    }
}
