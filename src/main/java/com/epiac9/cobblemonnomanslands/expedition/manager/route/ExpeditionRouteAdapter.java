package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionMapping;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionProfile;

import java.util.UUID;

public class ExpeditionRouteAdapter {
    private final ExpeditionDimensionMapping mapping;

    public ExpeditionRouteAdapter(ExpeditionDimensionMapping mapping) {
        this.mapping = mapping;
    }

    public ExpeditionDimensionMapping getMapping() {
        return mapping;
    }

    public DungeonRouteRequest createRequest(UUID ownerId, ExpeditionDefinition expedition) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId cannot be null");
        }
        if (expedition == null) {
            throw new IllegalArgumentException("expedition cannot be null");
        }
        ExpeditionDimensionProfile profile = mapping.getProfile(expedition.getId());
        if (profile == null) {
            throw new IllegalArgumentException("No dimension profile exists for " + expedition.getId());
        }

        return new DungeonRouteRequest(
                ownerId,
                profile.dimensionId().toString(),
                expedition.getTier(),
                expedition.getDurationSeconds(),
                DungeonRouteRequest.RouteMode.EXPEDITION_DISPATCH);
    }
}
