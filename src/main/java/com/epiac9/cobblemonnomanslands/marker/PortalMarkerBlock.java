package com.epiac9.cobblemonnomanslands.marker;

import net.minecraft.core.BlockPos;

public class PortalMarkerBlock extends BaseMarkerBlock {
    public PortalMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MarkerData createMarkerData(BlockPos pos) {
        return new MarkerData(pos, "portal");
    }
}
