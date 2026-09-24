package com.epiac9.cobblemonnomanslands.expedition.selection;

import com.epiac9.cobblemonnomanslands.expedition.manager.route.DungeonRouteResult;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStats;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStatsService;
import com.epiac9.cobblemonnomanslands.portal.ExpeditionPortalCoordinator;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.ExpeditionRouteService;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ExplorationSelectionService {
    private static final double MAX_BOARD_DISTANCE_SQUARED = 64.0D;

    private final ExpeditionPortalCoordinator coordinator;
    private final ExplorationPlayerStatsService playerStatsService;
    private final Map<BoardKey, ExplorationSelectionState> activeSelections = new HashMap<>();

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

        ExplorationPlayerStats ownerStats = playerStatsService.get(owner);
        DungeonRouteResult route = coordinator.routeAndActivateExploration(
            owner, explorationId, ownerStats.partyCount(), boardPosition, activePortalState,
            ownerStats.power(), ownerStats.rank());
        if (!route.accepted()) {
            return SelectionResult.rejected(route.reason());
        }

        var profile = coordinator.getExplorationProfile(explorationId);
        int durationMinutes = profile == null ? 12 : profile.durationMinutesForRank(ownerStats.rank());
        long startedAt = owner.serverLevel().getGameTime();
        long expiresAt = startedAt + durationMinutes * 60L * 20L;
        activeSelections.put(new BoardKey(owner.serverLevel().dimension(), boardPosition),
            new ExplorationSelectionState(
                owner.serverLevel().dimension(), boardPosition, owner.getUUID(), explorationId,
                route.pendingInstanceId(), startedAt, expiresAt));

        return SelectionResult.acceptedResult(ownerStats, ownerStats.power(),
            profile == null ? 0 : profile.requiredPowerForRank(ownerStats.rank()),
            durationMinutes, startedAt, expiresAt,
            ExpeditionRouteService.maximumExplorationPokemon(ownerStats.rank()));
    }

    public ExplorationSelectionState get(ResourceKey<Level> dimension, BlockPos boardPosition) {
        if (dimension == null || boardPosition == null) {
            return null;
        }
        return activeSelections.get(new BoardKey(dimension, boardPosition));
    }

    public void clearAll() {
        activeSelections.clear();
    }

    public void cancel(ServerPlayer owner, BlockPos boardPosition) {
        if (owner == null || boardPosition == null) {
            return;
        }
        BoardKey key = new BoardKey(owner.serverLevel().dimension(), boardPosition);
        ExplorationSelectionState selection = activeSelections.get(key);
        if (selection != null && selection.ownerId().equals(owner.getUUID())) {
            activeSelections.remove(key);
            coordinator.deactivateExploration(owner, selection);
        }
    }

    public void cancelOwnedBy(ServerPlayer owner) {
        if (owner == null) {
            return;
        }
        List<BoardKey> ownedKeys = activeSelections.entrySet().stream()
            .filter(entry -> entry.getValue().ownerId().equals(owner.getUUID()))
            .map(Map.Entry::getKey)
            .toList();
        for (BoardKey key : ownedKeys) {
            ExplorationSelectionState selection = activeSelections.remove(key);
            if (selection != null) {
                coordinator.deactivateExploration(owner, selection);
            }
        }
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

    private record BoardKey(ResourceKey<Level> dimension, BlockPos boardPosition) {
        private BoardKey {
            boardPosition = boardPosition.immutable();
        }
    }
}
