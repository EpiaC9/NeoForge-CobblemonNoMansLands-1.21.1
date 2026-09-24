package com.epiac9.cobblemonnomanslands.expedition.selection;

import com.cobblemonexpeditions.block.ExpeditionBoardBlock;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStatsService;
import com.epiac9.cobblemonnomanslands.network.ExplorationPartyStatusPayload;
import com.epiac9.cobblemonnomanslands.network.ExplorationSelectionPayload;
import com.epiac9.cobblemonnomanslands.portal.ExpeditionPortalCoordinator;
import com.epiac9.cobblemonnomanslands.registry.ModBlocks;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnectionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/** Validates client board requests before any party lookup, routing, or world mutation. */
public final class ExplorationBoardService {
    private final ExplorationBoardRequestPolicy requests = new ExplorationBoardRequestPolicy();
    private final RoomConnectionRegistry connections;
    private final ExpeditionPortalCoordinator coordinator;
    private final ExplorationSelectionService selections;
    private final ExplorationPlayerStatsService stats;

    public ExplorationBoardService(RoomConnectionRegistry connections, ExpeditionPortalCoordinator coordinator,
                                    ExplorationSelectionService selections, ExplorationPlayerStatsService stats) {
        this.connections = connections;
        this.coordinator = coordinator;
        this.selections = selections;
        this.stats = stats;
    }

    public void requestStatus(ServerPlayer player, BlockPos board) {
        if (allow(player, board, ExplorationBoardRequestPolicy.Action.STATUS)) {
            sendStatus(player, board);
        }
    }

    public void select(ServerPlayer player, BlockPos board, ResourceLocation explorationId) {
        if (explorationId == null || !allow(player, board, ExplorationBoardRequestPolicy.Action.SELECT)) {
            return;
        }
        ExplorationSelectionService.SelectionResult result = canAccess(player, board)
            ? selections.select(player, explorationId, board, ModBlocks.DUNGEON_PORTAL.get().defaultBlockState())
            : ExplorationSelectionService.SelectionResult.rejected("Board is unavailable or too far away");
        PacketDistributor.sendToPlayer(player, ExplorationSelectionPayload.buildResult(
            player.serverLevel().dimension().location(), board, explorationId, result));
        sendStatus(player, board);
    }

    public void cancel(ServerPlayer player, BlockPos board) {
        if (!allow(player, board, ExplorationBoardRequestPolicy.Action.CANCEL)) {
            return;
        }
        if (canAccess(player, board)) {
            selections.cancel(player, board);
        }
        // Report what is actually reserved, including a failed/no-op cancellation.
        sendStatus(player, board);
    }

    public void forgetPlayer(UUID ownerId) {
        requests.forget(ownerId);
    }

    private boolean allow(ServerPlayer player, BlockPos board, ExplorationBoardRequestPolicy.Action action) {
        return player != null && board != null
            && requests.tryAcquire(player.getUUID(), action, player.server.overworld().getGameTime());
    }

    private boolean canAccess(ServerPlayer player, BlockPos board) {
        var level = player.serverLevel();
        return ExplorationBoardRequestPolicy.canAccess(
            player.distanceToSqr(board.getX() + 0.5D, board.getY() + 0.5D, board.getZ() + 0.5D),
            connections.findByBoard(level.dimension(), board) != null,
            () -> level.hasChunkAt(board),
            () -> level.getBlockState(board).getBlock() instanceof ExpeditionBoardBlock);
    }

    private void sendStatus(ServerPlayer player, BlockPos board) {
        ResourceLocation dimension = player.serverLevel().dimension().location();
        ExplorationPartyStatusPayload snapshot;
        if (!canAccess(player, board)) {
            snapshot = ExplorationPartyStatusPayload.unavailable(dimension, board);
        } else {
            snapshot = ExplorationPartyStatusPayload.snapshot(dimension, board, stats.get(player),
                coordinator.hasAvailablePortal(player.serverLevel(), board),
                selections.getPending(player.getUUID()));
        }
        PacketDistributor.sendToPlayer(player, snapshot);
    }
}
