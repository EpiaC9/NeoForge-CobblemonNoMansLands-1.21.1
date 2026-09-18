package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.ExpeditionDungeonMapping;
import com.epiac9.cobblemonnomanslands.expedition.manager.DungeonLotManager;
import com.epiac9.cobblemonnomanslands.expedition.manager.gate.ExpeditionGatePolicy;
import com.epiac9.cobblemonnomanslands.expedition.manager.instance.DungeonInstanceManager;

import java.util.Set;
import java.util.UUID;

public class ExpeditionRouteService {
    private final ExpeditionRouteAdapter adapter;
    private final DungeonRouteManager routeManager;
    private final DungeonLotManager lotManager;
    private final DungeonInstanceManager instanceManager;
    private final ExpeditionGatePolicy gatePolicy;
    //connects mappings to adapter

    public ExpeditionRouteService(int lotCount, Set<String> validDungeons) {
        this.lotManager = new DungeonLotManager(lotCount);
        this.instanceManager = new DungeonInstanceManager(lotManager);
        this.routeManager = new DungeonRouteManager(validDungeons);

        ExpeditionDungeonMapping mapping = InitialExpeditionMappings.create();
        this.adapter = new ExpeditionRouteAdapter(mapping);
        this.gatePolicy = new ExpeditionGatePolicy(mapping);
    }

    public DungeonRouteResult routeExpedition(UUID ownerId, ExpeditionDefinition expedition, int selectedPokemonCount) {
        if (!gatePolicy.canRoute(expedition)) {
            return new DungeonRouteResult(false,null,null,"Expedition is not enabled for routing",null,null);
        }
        if (!gatePolicy.isPartySizeAllowed(expedition, selectedPokemonCount)) {
            return new DungeonRouteResult(false,null,null,"Party size is over the allowed limit",null,null);
        }

        DungeonRouteRequest request = adapter.createRequest(ownerId, expedition);
        return routeManager.route(request, lotManager, instanceManager);
    }
}
