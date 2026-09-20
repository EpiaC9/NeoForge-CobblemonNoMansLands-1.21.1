package com.epiac9.cobblemonnomanslands.dimension;

import com.epiac9.cobblemonnomanslands.dungeon.lot.DungeonLot;
import net.minecraft.core.BlockPos;

public record DungeonBounds(BlockPos origin, int width, int height, int depth) {
    public DungeonBounds {
        if (origin == null) {
            throw  new NullPointerException("origin is null");
        }
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw  new IllegalArgumentException("dungeon dimension must be positive");
        }

        origin = origin.immutable();
    }

    public BlockPos max() {
        return origin.offset(width - 1, height - 1, depth - 1);
    }

    public boolean fitsInside(DungeonLot lot) {
        if (lot == null) {
            return false;
        }

        return lot.contains(origin) && lot.contains(max());
    }

    public int minChunkX() {
        return origin.getX() >> 4;
    }

    public int maxChunkX() {
        return max().getX() >> 4;
    }

    public int minChunkZ() {
        return origin.getZ() >> 4;
    }

    public int maxChunkZ() {
        return max().getZ() >> 4;
    }

    public int chunkCount() {
        return (maxChunkX() - minChunkX() + 1) * (maxChunkZ() - minChunkZ() + 1);
    }
}
