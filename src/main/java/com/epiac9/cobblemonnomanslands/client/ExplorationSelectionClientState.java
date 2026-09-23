package com.epiac9.cobblemonnomanslands.client;

import com.epiac9.cobblemonnomanslands.network.ExplorationSelectionResultPayload;
import net.minecraft.core.BlockPos;

public final class ExplorationSelectionClientState {
    private static ExplorationSelectionResultPayload result;

    private ExplorationSelectionClientState() {
    }

    public static synchronized void accept(ExplorationSelectionResultPayload response) {
        result = response;
    }

    public static synchronized ExplorationSelectionResultPayload peek(BlockPos boardPosition) {
        if (result == null || !result.boardPosition().equals(boardPosition)) {
            return null;
        }
        return result;
    }

    public static synchronized void clear() {
        result = null;
    }
}