package com.epiac9.cobblemonnomanslands.portal.runtime;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

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
        if (runtime != null) {
            runtime.getExplorationSelectionService().clearAll(event.getServer());
        }
        runtime = null;
    } //event handler

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (runtime != null && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            runtime.getExplorationSelectionService().cancelOwnedBy(player);
            runtime.getBoardService().forgetPlayer(player.getUUID());
        }
    }

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

        runtime.tick(event.getServer());
    }
}
