package com.epiac9.cobblemonnomanslands.client;

import com.epiac9.cobblemonnomanslands.network.ExplorationPartyStatusPayload;
import net.minecraft.core.BlockPos;

public final class ExplorationPartyClientState {
    private static ExplorationPartyStatusPayload status;

    private ExplorationPartyClientState() {
    }

    public static synchronized void accept(ExplorationPartyStatusPayload payload) {
        status = payload;
    }

    public static synchronized ExplorationPartyStatusPayload peek(BlockPos boardPosition) {
        return status != null && status.boardPosition().equals(boardPosition) ? status : null;
    }

    public static synchronized void clear() {
        status = null;
    }
}
