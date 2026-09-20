package com.epiac9.cobblemonnomanslands.marker.room;

import com.epiac9.cobblemonnomanslands.marker.base.BaseMarkerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class RoomMarkerBlock extends Block {
    public RoomMarkerBlock(Properties properties) {
        super(properties);
    }

    public RoomData recordRoom(BlockPos markerPosition, BlockPos origin, BlockPos size,
                               List<BaseMarkerBlock.MarkerData> markers) {
        return new RoomData(markerPosition, origin, size, markers);
    }

    public record RoomData(BlockPos markerPosition, BlockPos origin, BlockPos size,
                           List<BaseMarkerBlock.MarkerData> markers) {
        public RoomData {
            markerPosition = markerPosition.immutable();
            origin = origin.immutable();
            size = size.immutable();
            markers = List.copyOf(markers);
        }

        public BlockPos max() {
            return origin.offset(
                    size.getX() - 1,
                    size.getY() - 1,
                    size.getZ() - 1
            );
        }
    }

    public boolean removeMarker(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        if (level.getBlockState(pos).getBlock() != this) {
            return false;
        }
        level.removeBlock(pos, false);
        return true;
    }
}
