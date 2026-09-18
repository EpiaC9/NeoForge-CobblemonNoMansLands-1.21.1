package com.epiac9.cobblemonnomanslands.marker;

import net.minecraft.core.BlockPos;

public class StarterStructureMarker extends BaseMarkerBlock {
    public StarterStructureMarker(Properties properties) {
        super(properties);
    }

    @Override
    public MarkerData createMarkerData(BlockPos pos) {
        return new MarkerData(pos, "spawn");
    }
}
