package com.epiac9.cobblemonnomanslands.dungeon.lot;

import net.minecraft.core.BlockPos;

public record DungeonLot(int lotId, BlockPos origin, int width, int height, int depth) {
    public DungeonLot {
        if (lotId < 0) {
            throw new IllegalArgumentException("lotId cannot be negative");
        }
        if (origin == null) {
            throw new IllegalArgumentException("origin cannot be null");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("width cannot be negative");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height cannot be negative");
        }
        if (depth <= 0) {
            throw new IllegalArgumentException("depth cannot be negative");
        }
        origin = origin.immutable();
    }

    public BlockPos max() {
        return origin.offset(width -1, height - 1, depth -1);
    }

    public long volume() {
        return (long) width * height * depth;
    }

    public boolean contains(BlockPos position) {
        if (position == null) {
            return false;
        }

        return position.getX() >= origin.getX()
                && position.getX() <= max().getX()
                && position.getY() >= origin.getY()
                && position.getY() <= max().getY()
                && position.getZ() >= origin.getZ()
                && position.getZ() <= max().getZ();
    }
}
