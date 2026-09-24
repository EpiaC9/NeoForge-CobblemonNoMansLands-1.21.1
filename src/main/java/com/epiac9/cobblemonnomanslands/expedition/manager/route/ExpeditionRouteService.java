package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionMapping;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionProfile;
import com.epiac9.cobblemonnomanslands.expedition.manager.gate.ExpeditionGatePolicy;
import com.epiac9.cobblemonnomanslands.expedition.manager.instance.DungeonInstanceManager;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public class ExpeditionRouteService {
    public static int maximumExplorationPokemon(int ownerRank) {
        return Math.max(1, Math.min(6, 1 + Math.floorDiv(Math.max(0, Math.min(10, ownerRank)) * 5, 10)));
    }

    private final ExpeditionRouteAdapter adapter;
    private final DungeonRouteManager routeManager;
    private final DungeonInstanceManager instanceManager;
    private final ExpeditionGatePolicy gatePolicy;
    //connects mappings to adapter

    public ExpeditionRouteService(ExpeditionDimensionMapping mapping) {
        this.instanceManager = new DungeonInstanceManager();
        this.routeManager = new DungeonRouteManager(mapping.dimensionKeys());
        this.adapter = new ExpeditionRouteAdapter(mapping);
        this.gatePolicy = new ExpeditionGatePolicy(mapping);
    }

    public DungeonRouteResult routeExpedition(UUID ownerId, ExpeditionDefinition expedition, int selectedPokemonCount) {
        if (!gatePolicy.canRoute(expedition)) {
            return DungeonRouteResult.rejected("Expedition is not enabled for routing");
        }
        if (!gatePolicy.isPartySizeAllowed(expedition, selectedPokemonCount)) {
            return DungeonRouteResult.rejected("Party size is over the allowed limit");
        }

        DungeonRouteRequest request = adapter.createRequest(ownerId, expedition);
        return routeManager.route(request, instanceManager);
    }

    public DungeonRouteResult routeExploration(UUID ownerId, ResourceLocation explorationId, int partySize,
                                               int currentPower, int ownerRank) {
        ExpeditionDimensionProfile profile = adapter.getMapping().getProfile(explorationId);
        if (profile == null) {
            return DungeonRouteResult.rejected("Exploration is not registered");
        }
        if (partySize < 1) {
            return DungeonRouteResult.rejected(profile.dimensionId().toString(), "Add pokemon to your party!");
        }
        if (partySize > profile.maxMembers()) {
            return DungeonRouteResult.rejected(profile.dimensionId().toString(), "Exploration party size is over the allowed limit");
        }
        int maximumPokemon = maximumExplorationPokemon(ownerRank);
        if (partySize > maximumPokemon) {
            return DungeonRouteResult.rejected(profile.dimensionId().toString(),
                "Max " + maximumPokemon + " pokemon!");
        }
        if (currentPower < profile.requiredPowerForRank(ownerRank)) {
            return DungeonRouteResult.rejected(profile.dimensionId().toString(), "Power requirement not met");
        }

        DungeonRouteRequest request = new DungeonRouteRequest(
                ownerId,
                profile.dimensionId().toString(),
                1,
                1,
                DungeonRouteRequest.RouteMode.EXPEDITION_DISPATCH
        );
        return routeManager.route(request, instanceManager);
    }

    public DungeonInstanceManager getInstanceManager() {
        return instanceManager;
    }

    public ExpeditionDimensionProfile getExplorationProfile(ResourceLocation explorationId) {
        return adapter.getMapping().getProfile(explorationId);
    }
}
