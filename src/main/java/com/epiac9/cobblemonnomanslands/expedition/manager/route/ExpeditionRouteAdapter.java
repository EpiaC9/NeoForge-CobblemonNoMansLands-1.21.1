package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.ExpeditionDungeonMapping;

import java.util.UUID;

public class ExpeditionRouteAdapter {
    private final ExpeditionDungeonMapping mapping;

    public ExpeditionRouteAdapter(ExpeditionDungeonMapping mapping) {
        this.mapping = mapping;
    }

    public DungeonRouteRequest createRequest(UUID ownerId, ExpeditionDefinition expedition) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId cannot be null");
        }
        if (expedition == null) {
            throw new IllegalArgumentException("expedition cannot be null");
        }
        String dungeonKey = mapping.getDungeonKey(expedition.getId());
        if (dungeonKey == null) {
            throw new IllegalArgumentException("No dungeon mapping exists for " + expedition.getId());
        }

        return new DungeonRouteRequest(
                ownerId,
                dungeonKey,
                expedition.getTier(),
                expedition.getDurationSeconds(),
                DungeonRouteRequest.RouteMode.EXPEDITION_DISPATCH,
                expedition.getRequiredPower(),
                expedition.getMinPartySize(),
                expedition.getMaxPartySize());
    }
}
