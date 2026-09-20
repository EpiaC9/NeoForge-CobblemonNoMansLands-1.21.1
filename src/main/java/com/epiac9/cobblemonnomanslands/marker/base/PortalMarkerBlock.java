package com.epiac9.cobblemonnomanslands.marker.base;

import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalService;
import com.epiac9.cobblemonnomanslands.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class PortalMarkerBlock extends BaseMarkerBlock {
    public PortalMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MarkerData createMarkerData(BlockPos pos) {
        return new MarkerData(pos, "portal");
    }

    public PortalAnchorState placePortal(ServerLevel level, BlockPos center) {
        if (level == null || center == null) {
            return null;
        }

        PortalAnchorState anchor = new PortalAnchorState(center);
        BlockState inactivePortalState = ModBlocks.DUNGEON_PORTAL.get().defaultBlockState();

        new PortalService().createInactivePortal(level, anchor, inactivePortalState);
        return anchor;
    }
}
