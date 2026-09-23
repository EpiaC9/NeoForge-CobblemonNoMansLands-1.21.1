package com.epiac9.cobblemonnomanslands.expedition.selection;

import com.epiac9.cobblemonnomanslands.portal.ExpeditionPortalCoordinator;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStatsService;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStats;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.DungeonRouteResult;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public final class ExplorationSelectionService {
    private static final int MAX_TEAMMATES = 3;
    private static final double MAX_BOARD_DISTANCE_SQUARED = 64.0D;

    private final ExpeditionPortalCoordinator coordinator;
    private final ExplorationPlayerStatsService playerStatsService;
    private final Map<BoardKey, ExplorationSelectionState> activeSelections = new HashMap<>();

    public ExplorationSelectionService(ExpeditionPortalCoordinator coordinator,
                                       ExplorationPlayerStatsService playerStatsService) {
        if (coordinator == null) {
            throw new IllegalArgumentException("coordinator cannot be null");
        }
        this.coordinator = coordinator;
        if (playerStatsService == null) {
            throw new IllegalArgumentException("playerStatsService cannot be null");
        }
        this.playerStatsService = playerStatsService;
    }

    public SelectionResult select(ServerPlayer owner, ResourceLocation explorationId,
                                  BlockPos boardPosition, List<UUID> teammateIds,
                                  BlockState activePortalState) {
        if (owner == null || explorationId == null || boardPosition == null
                || teammateIds == null || activePortalState == null) {
            return SelectionResult.rejected("Exploration selection was incomplete");
        }
        if (owner.distanceToSqr(boardPosition.getX() + 0.5D, boardPosition.getY() + 0.5D,
                boardPosition.getZ() + 0.5D) > MAX_BOARD_DISTANCE_SQUARED) {
            return SelectionResult.rejected("You are too far from the board");
        }
        if (teammateIds.size() > MAX_TEAMMATES || new HashSet<>(teammateIds).size() != teammateIds.size()
                || teammateIds.contains(owner.getUUID())) {
            return SelectionResult.rejected("Invalid exploration members");
        }

        BoardKey key = new BoardKey(owner.serverLevel().dimension(), boardPosition);

        for (UUID teammateId : teammateIds) {
            ServerPlayer teammate = owner.server.getPlayerList().getPlayer(teammateId);
            if (teammate == null || teammate.serverLevel() != owner.serverLevel()) {
                return SelectionResult.rejected("A teammate is unavailable");
            }
        }

        ExplorationPlayerStats ownerStats = playerStatsService.get(owner);
        int currentPower = ownerStats.power();
        List<ExplorationPlayerStats> memberStats = new ArrayList<>();
        memberStats.add(ownerStats);
        for (UUID teammateId : teammateIds) {
            ServerPlayer teammate = owner.server.getPlayerList().getPlayer(teammateId);
            ExplorationPlayerStats teammateStats = playerStatsService.get(teammate);
            memberStats.add(teammateStats);
            currentPower += teammateStats.power();
        }
        DungeonRouteResult result = coordinator.routeAndActivateExploration(
                owner,
                explorationId,
                teammateIds.size() + 1,
                boardPosition,
            activePortalState,
                currentPower,
                ownerStats.rank()
        );
        if (!result.accepted()) {
            return SelectionResult.rejected(result.reason());
        }

        activeSelections.put(key, new ExplorationSelectionState(
                owner.serverLevel().dimension(),
                boardPosition,
                owner.getUUID(),
                explorationId,
                teammateIds,
                result.pendingInstanceId()
        ));
        var profile = coordinator.getExplorationProfile(explorationId);
        return SelectionResult.acceptedResult(ownerStats, memberStats, currentPower,
            profile == null ? 0 : profile.requiredPowerForRank(ownerStats.rank()),
            profile == null ? 1 : profile.maxMembers());
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
                                  List<ExplorationPlayerStats> memberStats,
                                  int currentPower, int requiredPower, int maxMembers) {
        public static SelectionResult acceptedResult(ExplorationPlayerStats ownerStats,
                                                      List<ExplorationPlayerStats> memberStats,
                                                      int currentPower, int requiredPower, int maxMembers) {
            return new SelectionResult(true, "Exploration portal activated", ownerStats,
                    List.copyOf(memberStats), currentPower, requiredPower, maxMembers);
        }

        public static SelectionResult rejected(String reason) {
            return new SelectionResult(false, reason, new ExplorationPlayerStats(0, 0),
                    List.of(), 0, 0, 1);
        }
    }

    private record BoardKey(ResourceKey<Level> dimension, BlockPos boardPosition) {
        private BoardKey {
            boardPosition = boardPosition.immutable();
        }
    }
}
