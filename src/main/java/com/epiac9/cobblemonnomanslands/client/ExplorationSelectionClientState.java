package com.epiac9.cobblemonnomanslands.client;

import com.epiac9.cobblemonnomanslands.network.ExplorationSelectionResultPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class ExplorationSelectionClientState {
    private static ExplorationSelectionResultPayload result;
    private static ResourceLocation watchedDimension;
    private static BlockPos watchedBoard;

    private ExplorationSelectionClientState() {
    }

    public static synchronized void accept(ExplorationSelectionResultPayload response) {
        if (response.dimensionId().equals(watchedDimension) && response.boardPosition().equals(watchedBoard)) {
            result = response;
        }
    }

    public static synchronized ExplorationSelectionResultPayload peek(ResourceLocation dimension, BlockPos boardPosition) {
        if (result == null || !result.dimensionId().equals(dimension) || !result.boardPosition().equals(boardPosition)) {
            return null;
        }
        return result;
    }

    public static synchronized void begin(ResourceLocation dimension, BlockPos boardPosition) {
        watchedDimension = dimension;
        watchedBoard = boardPosition.immutable();
        result = null;
    }

    public static synchronized void clear() {
        result = null;
        watchedDimension = null;
        watchedBoard = null;
    }
}
