package com.epiac9.cobblemonnomanslands.dungeon.lot;

import net.minecraft.core.BlockPos;

import java.util.Map;

public final class DungeonLotRegistry {
    public static final int LOT_SIZE = 256;
    public static final int LOT_SPACING = 512;
    public static final int LOT_MIN_Y = -64;
    public static final int LOT_MAX_Y = 191;

    private static final Map<Integer, DungeonLot> LOTS = Map.of(
            0, createLot(0, 0, 0),
            1, createLot(1, LOT_SPACING, 0),
            2, createLot(2, LOT_SPACING *2, 0),
            3, createLot(3, LOT_SPACING *3, 0)
    );

    private DungeonLotRegistry() {
    }

    public static DungeonLot get(int lotId) {
        return LOTS.get(lotId);
    }

    public static Map<Integer, DungeonLot> all() {
        return LOTS;
    }

    private static DungeonLot createLot(int lotId, int x, int z) {
        return new DungeonLot(lotId, new BlockPos(x, LOT_MIN_Y, z), LOT_SIZE, LOT_SIZE, LOT_SIZE);
    }
}
