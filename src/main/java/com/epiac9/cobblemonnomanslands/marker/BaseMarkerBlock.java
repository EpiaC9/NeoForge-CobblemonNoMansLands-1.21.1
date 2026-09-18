package com.epiac9.cobblemonnomanslands.marker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseMarkerBlock extends Block {

    protected BaseMarkerBlock(Properties properties) {
        super(properties);
    }

    public record MarkerData(BlockPos pos, String role) {
        public MarkerData {
            pos = pos.immutable();
        }
    }

    public MarkerData createMarkerData(BlockPos pos) {
        return new MarkerData(pos, "generic");
    }

    public boolean consume(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }

        BlockState current = level.getBlockState(pos);
        if (current.getBlock() != this) {
            return false;
        }

        level.removeBlock(pos, false);
        return true;
    }
}
