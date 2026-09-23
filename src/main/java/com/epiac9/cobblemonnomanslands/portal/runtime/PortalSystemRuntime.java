package com.epiac9.cobblemonnomanslands.portal.runtime;

import com.epiac9.cobblemonnomanslands.dimension.ExpeditionResourceReplenishmentService;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionMapping;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.InitialExpeditionMappings;
import com.epiac9.cobblemonnomanslands.expedition.selection.ExplorationSelectionService;
import com.epiac9.cobblemonnomanslands.portal.ExpeditionPortalCoordinator;
import com.epiac9.cobblemonnomanslands.structure.WorldSpawnStructureService;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnectionRegistry;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStatsService;
import net.minecraft.server.level.ServerLevel;

public class PortalSystemRuntime {
    private final ExpeditionPortalCoordinator coordinator;
    private final ExpeditionResourceReplenishmentService resourceReplenishmentService;
    private final ExplorationSelectionService explorationSelectionService;
    private final ExplorationPlayerStatsService playerStatsService;

    public PortalSystemRuntime() {
        ExpeditionDimensionMapping mapping = InitialExpeditionMappings.create();
        RoomConnectionRegistry connectionRegistry = WorldSpawnStructureService.getConnectionRegistry();
        this.coordinator = new ExpeditionPortalCoordinator(mapping, connectionRegistry);
        this.playerStatsService = new ExplorationPlayerStatsService();
        this.resourceReplenishmentService = new ExpeditionResourceReplenishmentService();
        this.explorationSelectionService = new ExplorationSelectionService(coordinator, playerStatsService);
    } //assign profile ids to portal

    public ExpeditionPortalCoordinator getCoordinator() {
        return coordinator;
    }
    public ExpeditionResourceReplenishmentService getResourceReplenishmentService() {
        return resourceReplenishmentService;
    }

    public ExplorationSelectionService getExplorationSelectionService() {
        return explorationSelectionService;
    }

    public ExplorationPlayerStatsService getPlayerStatsService() {
        return playerStatsService;
    }

    public void tick(ServerLevel expeditionLevel) {
        resourceReplenishmentService.tick(expeditionLevel);
    }
}
