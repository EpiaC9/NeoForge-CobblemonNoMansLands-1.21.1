package com.epiac9.cobblemonnomanslands.client;

import com.epiac9.cobblemonnomanslands.network.ExplorationPartyStatusPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class ExplorationPartyClientState {
    private static ExplorationPartyStatusPayload status;
    private static ResourceLocation watchedDimension;
    private static BlockPos watchedBoard;

    private ExplorationPartyClientState() {
    }

    public static synchronized void accept(ExplorationPartyStatusPayload payload) {
        if (payload.dimensionId().equals(watchedDimension) && payload.boardPosition().equals(watchedBoard)) {
            status = payload;
        }
    }

    public static synchronized ExplorationPartyStatusPayload peek(ResourceLocation dimension, BlockPos boardPosition) {
        return status != null && status.dimensionId().equals(dimension)
            && status.boardPosition().equals(boardPosition) ? status : null;
    }

    public static synchronized void begin(ResourceLocation dimension, BlockPos boardPosition) {
        watchedDimension = dimension;
        watchedBoard = boardPosition.immutable();
        status = null;
    }

    public static synchronized void clear() {
        status = null;
        watchedDimension = null;
        watchedBoard = null;
    }
}
