package com.epiac9.cobblemonnomanslands.expedition.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public record ExplorationSelectionState(ResourceKey<Level> dimension, BlockPos boardPosition,
                                        UUID ownerId, ResourceLocation explorationId,
                                        String instanceId, long startedAt, long expiresAt) {
    public ExplorationSelectionState {
        if (dimension == null || boardPosition == null || ownerId == null || explorationId == null
                    || instanceId == null) {
            throw new IllegalArgumentException("Exploration selection fields cannot be null");
        }
        boardPosition = boardPosition.immutable();
        if (startedAt < 0 || expiresAt < startedAt) {
            throw new IllegalArgumentException("Invalid exploration session times");
        }
    }
}