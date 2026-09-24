package com.epiac9.cobblemonnomanslands.expedition.selection;

import com.epiac9.cobblemonnomanslands.expedition.manager.route.DungeonRouteResult;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStats;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStatsService;
import com.epiac9.cobblemonnomanslands.portal.ExpeditionPortalCoordinator;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.ExpeditionRouteService;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public final class ExplorationSelectionService {
    private static final double MAX_BOARD_DISTANCE_SQUARED = 64.0D;

    private final ExpeditionPortalCoordinator coordinator;
    private final ExplorationPlayerStatsService playerStatsService;
    private final ExplorationReservationRegistry reservations = new ExplorationReservationRegistry();

    public ExplorationSelectionService(ExpeditionPortalCoordinator coordinator,
                                       ExplorationPlayerStatsService playerStatsService) {
        this.coordinator = coordinator;
        this.playerStatsService = playerStatsService;
    }

    public SelectionResult select(ServerPlayer owner, ResourceLocation explorationId,
                                  BlockPos boardPosition, BlockState activePortalState) {
        if (owner == null || explorationId == null || boardPosition == null || activePortalState == null) {
            return SelectionResult.rejected("Exploration selection was incomplete");
        }
        if (owner.distanceToSqr(boardPosition.getX() + 0.5D, boardPosition.getY() + 0.5D,
                boardPosition.getZ() + 0.5D) > MAX_BOARD_DISTANCE_SQUARED) {
            return SelectionResult.rejected("You are too far from the board");
        }

        tick(owner.server);
        if (reservations.get(owner.getUUID()) != null) {
            return SelectionResult.rejected("You already have a pending portal.");
        }
        ExplorationPlayerStats ownerStats = playerStatsService.get(owner);
        if (!ownerStats.available()) {
            return SelectionResult.rejected("Party stats are temporarily unavailable. Please try again.");
        }
        DungeonRouteResult route = coordinator.routeAndActivateExploration(
            owner, explorationId, ownerStats.partyCount(), boardPosition, activePortalState,
            ownerStats.power(), ownerStats.rank());
        if (!route.accepted()) {
            return SelectionResult.rejected(route.reason());
        }

        var profile = coordinator.getExplorationProfile(explorationId);
        int durationMinutes = profile == null ? 12 : profile.durationMinutesForRank(ownerStats.rank());
        long startedAt = owner.server.overworld().getGameTime();
        long expiresAt = startedAt + durationMinutes * 60L * 20L;
        ExplorationSelectionState selection = new ExplorationSelectionState(
                owner.serverLevel().dimension(), boardPosition, owner.getUUID(), explorationId,
                route.pendingInstanceId(), startedAt, expiresAt);
        if (!reservations.reserve(selection)) {
            coordinator.deactivateExploration(owner.server, selection);
            return SelectionResult.rejected("You already have a pending portal.");
        }

        return SelectionResult.acceptedResult(ownerStats, ownerStats.power(),
            profile == null ? 0 : profile.requiredPowerForRank(ownerStats.rank()),
            durationMinutes, startedAt, expiresAt,
            ExpeditionRouteService.maximumExplorationPokemon(ownerStats.rank()));
    }

    public void tick(MinecraftServer server) {
        reservations.expire(server.overworld().getGameTime(),
            selection -> coordinator.deactivateExploration(server, selection));
    }

    public ExplorationSelectionState getPending(UUID ownerId) {
        return reservations.get(ownerId);
    }

    public void clearAll(MinecraftServer server) {
        reservations.releaseAll(selection -> coordinator.deactivateExploration(server, selection));
    }

    public void cancel(ServerPlayer owner, BlockPos boardPosition) {
        if (owner == null || boardPosition == null) {
            return;
        }
        ExplorationSelectionState selection = reservations.get(owner.getUUID());
        if (selection != null && selection.dimension().equals(owner.serverLevel().dimension())
                && selection.boardPosition().equals(boardPosition)) {
            release(owner.server, selection);
        }
    }

    public void cancelOwnedBy(ServerPlayer owner) {
        if (owner == null) {
            return;
        }
        ExplorationSelectionState selection = reservations.get(owner.getUUID());
        if (selection != null) {
            release(owner.server, selection);
        }
    }

    private void release(MinecraftServer server, ExplorationSelectionState selection) {
        reservations.release(selection.ownerId(), selection.instanceId(),
            pending -> coordinator.deactivateExploration(server, pending));
    }

    public boolean enterPortal(ServerPlayer owner, ServerLevel sourceLevel, BlockPos portalPosition) {
        if (owner == null || sourceLevel == null || portalPosition == null) {
            return false;
        }
        ExplorationSelectionState selection = reservations.get(owner.getUUID());
        if (selection != null && owner.server.overworld().getGameTime() >= selection.expiresAt()) {
            release(owner.server, selection);
            return false;
        }
        if (!coordinator.enterPortal(owner, sourceLevel, portalPosition)) {
            return false;
        }
        if (selection != null) {
            reservations.complete(selection.ownerId(), selection.instanceId());
        }
        return true;
    }

    public record SelectionResult(boolean accepted, String reason,
                                  ExplorationPlayerStats ownerStats,
                                  int currentPower, int requiredPower, int durationMinutes,
                                  long startedAt, long expiresAt, int maximumPokemon) {
        public static SelectionResult acceptedResult(ExplorationPlayerStats ownerStats,
                                                      int currentPower, int requiredPower,
                                                      int durationMinutes, long startedAt, long expiresAt,
                                                      int maximumPokemon) {
            return new SelectionResult(true, "Exploration portal activated", ownerStats,
                currentPower, requiredPower, durationMinutes, startedAt, expiresAt, maximumPokemon);
        }

        public static SelectionResult rejected(String reason) {
            return new SelectionResult(false, reason, new ExplorationPlayerStats(0, 0, 0),
                0, 0, 12, 0, 0, 1);
        }
    }

}
