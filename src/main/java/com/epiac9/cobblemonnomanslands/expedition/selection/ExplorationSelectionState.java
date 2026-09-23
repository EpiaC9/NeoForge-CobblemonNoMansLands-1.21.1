package com.epiac9.cobblemonnomanslands.expedition.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public record ExplorationSelectionState(ResourceKey<Level> dimension, BlockPos boardPosition,
                                        UUID ownerId, ResourceLocation explorationId,
                                           List<UUID> teammateIds, String instanceId) {
    public ExplorationSelectionState {
        if (dimension == null || boardPosition == null || ownerId == null || explorationId == null
                    || teammateIds == null || instanceId == null) {
            throw new IllegalArgumentException("Exploration selection fields cannot be null");
        }
        boardPosition = boardPosition.immutable();
        teammateIds = List.copyOf(teammateIds);
    }
}