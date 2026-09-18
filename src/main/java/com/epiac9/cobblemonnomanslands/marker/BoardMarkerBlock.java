package com.epiac9.cobblemonnomanslands.marker;

import net.minecraft.core.BlockPos;

public class BoardMarkerBlock extends BaseMarkerBlock {
    public BoardMarkerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MarkerData createMarkerData(BlockPos pos) {
        return new MarkerData(pos, "board");
    }
}
