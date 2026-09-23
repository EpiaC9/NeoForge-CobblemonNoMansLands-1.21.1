package com.epiac9.cobblemonnomanslands.portal;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionMapping;
import com.epiac9.cobblemonnomanslands.expedition.manager.instance.DungeonInstanceManager;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.DungeonRouteResult;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.ExpeditionRouteService;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalService;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnection;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnectionRegistry;
import com.epiac9.cobblemonnomanslands.expedition.selection.ExplorationSelectionState;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import static com.epiac9.cobblemonnomanslands.expedition.manager.route.DungeonRouteResult.rejected;

public class ExpeditionPortalCoordinator {
    private final ExpeditionRouteService routeService;
    private final PortalService portalService;
    private final RoomConnectionRegistry connectionRegistry;
    //call routes and portal services

    public ExpeditionPortalCoordinator(ExpeditionDimensionMapping mapping, RoomConnectionRegistry connectionRegistry) {
        this.routeService = new ExpeditionRouteService(mapping);
        this.portalService = new PortalService();
        this.connectionRegistry = connectionRegistry;

        if (connectionRegistry == null) {
            throw new IllegalArgumentException("Connection registry cannot be null");
        }
    }

    public DungeonRouteResult routeAndActivate(ServerPlayer player, ExpeditionDefinition expedition, int selectedPokemonCount,
                                               BlockPos boardPosition, BlockState activePortalState) {
        if (player == null) {
            return rejected("Player was null");
        }
        if (expedition == null) {
            return rejected("Expedition was null");
        }
        if (boardPosition == null) {
            return rejected("Board position was null");
        }
        RoomConnection roomConnection = connectionRegistry.findByBoard(player.serverLevel().dimension(), boardPosition);
        if (roomConnection == null) {
            return rejected("Room connection was null");
        }
        if (activePortalState == null) {
            return rejected("Active portal state was null");
        }
        if (!roomConnection.containsBoard(boardPosition)) {
            return rejected("Board does not exist");
        }
        ServerLevel level = player.serverLevel();
        PortalAnchorState anchor = roomConnection.findAvailablePortal();
        if (anchor == null) {
            return rejected("No available portal was found");
        }
        DungeonRouteResult result = routeService.routeExpedition(player.getUUID(), expedition, selectedPokemonCount);
        if (!result.accepted()) {
            return result;
        }

        anchor.setInstanceId(result.pendingInstanceId());
        portalService.activatePortal(level, anchor, activePortalState);
        return result;
    }

    public DungeonRouteResult routeAndActivateExploration(ServerPlayer player, ResourceLocation explorationId,
                                                          int partySize, BlockPos boardPosition,
                                                          BlockState activePortalState, int currentPower,
                                                          int ownerRank) {
        if (player == null || explorationId == null || boardPosition == null || activePortalState == null) {
            return rejected("Exploration selection was incomplete");
        }
        RoomConnection roomConnection = connectionRegistry.findByBoard(player.serverLevel().dimension(), boardPosition);
        if (roomConnection == null || !roomConnection.containsBoard(boardPosition)) {
            return rejected("Board does not exist");
        }
        PortalAnchorState anchor = roomConnection.findAvailablePortal();
        if (anchor == null) {
            return rejected("No available portal was found");
        }
        DungeonRouteResult result = routeService.routeExploration(
                player.getUUID(), explorationId, partySize, currentPower, ownerRank);
        if (!result.accepted()) {
            return result;
        }

        anchor.setInstanceId(result.pendingInstanceId());
        portalService.activatePortal(player.serverLevel(), anchor, activePortalState);
        return result;
    }

    public boolean deactivateExploration(ServerPlayer owner, ExplorationSelectionState selection) {
        if (owner == null || selection == null) {
            return false;
        }
        RoomConnection roomConnection = connectionRegistry.findByBoard(
                selection.dimension(), selection.boardPosition());
        if (roomConnection == null) {
            return false;
        }
        PortalAnchorState anchor = roomConnection.findPortalByInstanceId(selection.instanceId());
        if (anchor == null) {
            return false;
        }
        portalService.deactivatePortal(
                owner.serverLevel(),
                anchor,
                com.epiac9.cobblemonnomanslands.registry.ModBlocks.DUNGEON_PORTAL.get().defaultBlockState());
        anchor.setInstanceId(null);
        return true;
    }

    public ExpeditionDimensionProfile getExplorationProfile(ResourceLocation explorationId) {
        return routeService.getExplorationProfile(explorationId);
    }

    public DungeonInstanceManager getInstanceManager() {
        return routeService.getInstanceManager();
    }

    public PortalService getPortalService() {
        return portalService;
    }

}
