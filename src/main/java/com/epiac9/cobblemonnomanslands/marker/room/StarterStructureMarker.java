package com.epiac9.cobblemonnomanslands.marker.room;


import com.epiac9.cobblemonnomanslands.marker.base.BaseMarkerBlock;
import net.minecraft.core.BlockPos;

import java.util.List;

public class StarterStructureMarker extends RoomMarkerBlock {
    public StarterStructureMarker(Properties properties) {
        super(properties);
    }

    public RoomData recordStructure(BlockPos markerPosition, BlockPos origin, BlockPos size,
                                    List<BaseMarkerBlock.MarkerData> markers) {
        return new RoomData(markerPosition, origin, size, markers);
    }
}
