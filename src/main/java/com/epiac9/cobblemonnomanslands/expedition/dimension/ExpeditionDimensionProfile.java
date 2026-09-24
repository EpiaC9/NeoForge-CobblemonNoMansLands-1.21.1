package com.epiac9.cobblemonnomanslands.expedition.dimension;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.Map;

public record ExpeditionDimensionProfile(ResourceLocation dimensionId, long replenishDelayTicks,
                                         Set<String> resourceCategories, Map<Integer, Integer> requiredPowerByRank,
                                         int maxMembers, Map<Integer, Integer> durationMinutesByRank) {
    public ExpeditionDimensionProfile {
        if (dimensionId == null) {
            throw new IllegalArgumentException("dimensionId cannot be null");
        }
        if (replenishDelayTicks <= 0) {
            throw new IllegalArgumentException("replenishDelayTicks must be positive");
        }
        if (resourceCategories == null) {
            throw new IllegalArgumentException("resourceCategories cannot be null");
        }
        if (requiredPowerByRank == null || requiredPowerByRank.isEmpty()) {
            throw new IllegalArgumentException("requiredPowerByRank cannot be empty");
        }
        if (maxMembers < 1) {
            throw new IllegalArgumentException("maxMembers must be positive");
        }
        if (durationMinutesByRank == null || durationMinutesByRank.isEmpty()) {
            throw new IllegalArgumentException("durationMinutesByRank cannot be empty");
        }
        resourceCategories = Set.copyOf(resourceCategories);
        requiredPowerByRank = Map.copyOf(requiredPowerByRank);
        durationMinutesByRank = Map.copyOf(durationMinutesByRank);
    }

    public int requiredPowerForRank(int rank) {
        int normalizedRank = Math.max(0, Math.min(10, rank));
        return requiredPowerByRank.getOrDefault(normalizedRank,
                requiredPowerByRank.getOrDefault(10, 0));
    }

    public int durationMinutesForRank(int rank) {
        int normalizedRank = Math.max(0, Math.min(10, rank));
        return durationMinutesByRank.getOrDefault(normalizedRank,
                durationMinutesByRank.getOrDefault(10, 12));
    }
}