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
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

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

    public boolean deactivateExploration(MinecraftServer server, ExplorationSelectionState selection) {
        if (server == null || selection == null) {
            return false;
        }
        RoomConnection roomConnection = connectionRegistry.findByBoard(
                selection.dimension(), selection.boardPosition());
        if (roomConnection == null) {
            routeService.getInstanceManager().removeInstance(selection.instanceId());
            return true;
        }
        PortalAnchorState anchor = roomConnection.findPortalByInstanceId(selection.instanceId());
        if (anchor == null) {
            routeService.getInstanceManager().removeInstance(selection.instanceId());
            return true;
        }
        ServerLevel sourceLevel = server.getLevel(selection.dimension());
        if (sourceLevel == null) {
            return false;
        }
        portalService.deactivatePortal(
                sourceLevel,
                anchor,
                com.epiac9.cobblemonnomanslands.registry.ModBlocks.DUNGEON_PORTAL.get().defaultBlockState());
        anchor.setInstanceId(null);
        routeService.getInstanceManager().removeInstance(selection.instanceId());
        return true;
    }

    public boolean enterPortal(ServerPlayer player, ServerLevel sourceLevel, BlockPos portalPosition) {
        if (player == null || sourceLevel == null || portalPosition == null) {
            return false;
        }
        if (player.serverLevel() != sourceLevel) {
            return false;
        }
        PortalAnchorState anchor = connectionRegistry.findPortalContaining(
            sourceLevel.dimension(), portalPosition);
        if (anchor == null || !anchor.isActive() || anchor.getInstanceId() == null) {
            return false;
        }
        var instance = routeService.getInstanceManager().getInstance(anchor.getInstanceId());
        if (instance == null || !player.getUUID().equals(instance.getOwnerId())) {
            return false;
        }
        ResourceKey<Level> targetKey = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.parse(instance.getDimensionKey()));
        ServerLevel targetLevel = player.server.getLevel(targetKey);
        if (targetLevel == null) {
            return false;
        }
        if (player.isOnPortalCooldown()) {
            return false;
        }
        // Failed terrain searches must not repeat every collision tick.
        player.setPortalCooldown();
        Vec3 arrival = PortalArrivalResolver.find(targetLevel, player);
        if (arrival == null) {
            player.displayClientMessage(Component.literal("No safe arrival spot was found near the destination spawn. Try again shortly."), true);
            return false;
        }
        var transferred = player.changeDimension(new DimensionTransition(
            targetLevel,
            arrival,
            Vec3.ZERO,
            targetLevel.getSharedSpawnAngle(),
            0.0F,
            DimensionTransition.DO_NOTHING
        ));
        if (transferred == null || player.serverLevel() != targetLevel) {
            return false;
        }
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        portalService.deactivatePortal(
            sourceLevel,
            anchor,
            com.epiac9.cobblemonnomanslands.registry.ModBlocks.DUNGEON_PORTAL.get().defaultBlockState());
        anchor.setInstanceId(null);
        routeService.getInstanceManager().removeInstance(instance.getInstanceId());
        return true;
    }

    public boolean hasAvailablePortal(ServerLevel level, BlockPos boardPosition) {
        if (level == null || boardPosition == null) {
            return false;
        }
        RoomConnection roomConnection = connectionRegistry.findByBoard(level.dimension(), boardPosition);
        return roomConnection != null && roomConnection.findAvailablePortal() != null;
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
