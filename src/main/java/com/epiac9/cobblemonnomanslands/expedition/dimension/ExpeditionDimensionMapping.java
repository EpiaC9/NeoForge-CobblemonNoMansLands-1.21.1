package com.epiac9.cobblemonnomanslands.expedition.dimension;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpeditionDimensionMapping {
    private final Map<ResourceLocation, ExpeditionDimensionProfile> expeditionToDimension;

    public ExpeditionDimensionMapping(Map<ResourceLocation, ExpeditionDimensionProfile> expeditionToDimension) {
        this.expeditionToDimension = new HashMap<>(expeditionToDimension);
    }

    public ExpeditionDimensionProfile getProfile(ResourceLocation expeditionId) {
        return expeditionToDimension.get(expeditionId);
    }

    public boolean contains(ResourceLocation expeditionId) {
        return expeditionToDimension.containsKey(expeditionId);
    }

    public Set<ResourceLocation> explorationIds() {
        return Set.copyOf(expeditionToDimension.keySet());
    }

    public Set<String> dimensionKeys() {
        return expeditionToDimension.values().stream()
                .map(profile -> profile.dimensionId().toString())
                .collect(Collectors.toUnmodifiableSet());
    }
}
