package com.epiac9.cobblemonnomanslands.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionProfile;

import java.util.HashMap;
import java.util.Map;

public final class ExpeditionResourceReplenishmentService {
    private final Map<ResourceKey<Level>, Long> emptySince = new HashMap<>();

    public void tick(ServerLevel level) {
        if (level == null) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        if (level.players().isEmpty()) {
            emptySince.putIfAbsent(dimension, level.getGameTime());
        } else {
            emptySince.remove(dimension);
        }
    }

    public Long getEmptySince(ResourceKey<Level> dimension) {
        return emptySince.get(dimension);
    }

    public boolean isReplenishmentDue(ServerLevel level, ExpeditionDimensionProfile profile) {
        if (level == null || profile == null || !level.players().isEmpty()) {
            return false;
        }

        Long startedEmptyAt = emptySince.get(level.dimension());
        return startedEmptyAt != null && level.getGameTime() - startedEmptyAt >= profile.replenishDelayTicks();
    }
}