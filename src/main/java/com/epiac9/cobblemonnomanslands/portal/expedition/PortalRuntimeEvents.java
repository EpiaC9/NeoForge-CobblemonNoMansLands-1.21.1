package com.epiac9.cobblemonnomanslands.portal.expedition;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

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
}
