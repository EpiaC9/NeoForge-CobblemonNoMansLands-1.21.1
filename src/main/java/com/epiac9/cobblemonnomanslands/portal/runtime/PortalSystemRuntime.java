package com.epiac9.cobblemonnomanslands.portal.runtime;

import com.epiac9.cobblemonnomanslands.dimension.ExpeditionResourceReplenishmentService;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionMapping;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.InitialExpeditionMappings;
import com.epiac9.cobblemonnomanslands.expedition.selection.ExplorationSelectionService;
import com.epiac9.cobblemonnomanslands.expedition.selection.ExplorationBoardService;
import com.epiac9.cobblemonnomanslands.portal.ExpeditionPortalCoordinator;
import com.epiac9.cobblemonnomanslands.structure.WorldSpawnStructureService;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnectionRegistry;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStatsService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.stream.Collectors;

public class PortalSystemRuntime {
    private final ExpeditionPortalCoordinator coordinator;
    private final ExpeditionResourceReplenishmentService resourceReplenishmentService;
    private final ExplorationSelectionService explorationSelectionService;
    private final ExplorationPlayerStatsService playerStatsService;
    private final Set<ResourceKey<Level>> expeditionDimensions;
    private final ExplorationBoardService boardService;

    public PortalSystemRuntime() {
        ExpeditionDimensionMapping mapping = InitialExpeditionMappings.create();
        this.expeditionDimensions = mapping.dimensionKeys().stream()
            .map(id -> ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(id)))
            .collect(Collectors.toUnmodifiableSet());
        RoomConnectionRegistry connectionRegistry = WorldSpawnStructureService.getConnectionRegistry();
        this.coordinator = new ExpeditionPortalCoordinator(mapping, connectionRegistry);
        this.playerStatsService = new ExplorationPlayerStatsService();
        this.resourceReplenishmentService = new ExpeditionResourceReplenishmentService();
        this.explorationSelectionService = new ExplorationSelectionService(coordinator, playerStatsService);
        this.boardService = new ExplorationBoardService(connectionRegistry, coordinator,
            explorationSelectionService, playerStatsService);
    } //assign profile ids to portal

    public ExpeditionPortalCoordinator getCoordinator() {
        return coordinator;
    }

    public ExplorationBoardService getBoardService() {
        return boardService;
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

    public void tick(MinecraftServer server) {
        explorationSelectionService.tick(server);
        for (ResourceKey<Level> dimension : expeditionDimensions) {
            ServerLevel level = server.getLevel(dimension);
            if (level != null) {
                resourceReplenishmentService.tick(level);
            }
        }
    }
}
