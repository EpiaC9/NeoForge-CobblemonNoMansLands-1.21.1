package com.epiac9.cobblemonnomanslands.structure;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = CobblemonNoMansLands.MODID)
public final class StarterStructureProtectionEvents {
    private StarterStructureProtectionEvents() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (WorldSpawnStructureService.getConnectionRegistry().isProtectedBlock(
                level.dimension(), event.getPos(), level.getBlockState(event.getPos()))) {
            event.setCanceled(true);
        }
    }
}
