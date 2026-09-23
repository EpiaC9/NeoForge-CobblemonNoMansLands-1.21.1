package com.epiac9.cobblemonnomanslands.network;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.expedition.selection.ExplorationSelectionService;
import com.epiac9.cobblemonnomanslands.portal.runtime.PortalRuntimeEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ExplorationSelectionPayload(BlockPos boardPosition, ResourceLocation explorationId,
                                          List<UUID> teammateIds) implements CustomPacketPayload {
    public static final Type<ExplorationSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonNoMansLands.MODID, "exploration_selection")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplorationSelectionPayload> STREAM_CODEC =
            StreamCodec.of(ExplorationSelectionPayload::write, ExplorationSelectionPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, ExplorationSelectionPayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.boardPosition());
        ResourceLocation.STREAM_CODEC.encode(buffer, payload.explorationId());
        buffer.writeVarInt(payload.teammateIds().size());
        for (UUID teammateId : payload.teammateIds()) {
            UUIDUtil.STREAM_CODEC.encode(buffer, teammateId);
        }
    }

    private static ExplorationSelectionPayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos boardPosition = BlockPos.STREAM_CODEC.decode(buffer);
        ResourceLocation explorationId = ResourceLocation.STREAM_CODEC.decode(buffer);
        int teammateCount = buffer.readVarInt();
        if (teammateCount < 0 || teammateCount > 3) {
            throw new IllegalArgumentException("Invalid teammate count");
        }

        List<UUID> teammateIds = new ArrayList<>(teammateCount);
        for (int index = 0; index < teammateCount; index++) {
            teammateIds.add(UUIDUtil.STREAM_CODEC.decode(buffer));
        }
        return new ExplorationSelectionPayload(boardPosition, explorationId, teammateIds);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            BlockState activePortalState = com.epiac9.cobblemonnomanslands.registry.ModBlocks.DUNGEON_PORTAL
                    .get().defaultBlockState();
                ExplorationSelectionService.SelectionResult result = PortalRuntimeEvents.getRuntime()
                    .getExplorationSelectionService().select(
                    player,
                    explorationId,
                    boardPosition,
                    teammateIds,
                    activePortalState
            );
                PacketDistributor.sendToPlayer(player, new ExplorationSelectionResultPayload(
                    boardPosition,
                        explorationId,
                    result.accepted(),
                        result.reason(),
                        result.currentPower(),
                        result.requiredPower(),
                        teammateIds.size() + 1,
                        result.maxMembers(),
                        buildPlayerStats(player, teammateIds, result)
                ));
        });
    }

                private static List<ExplorationSelectionResultPayload.PlayerStats> buildPlayerStats(
                    ServerPlayer owner, List<UUID> teammateIds,
                    ExplorationSelectionService.SelectionResult result) {
                List<ExplorationSelectionResultPayload.PlayerStats> stats = new ArrayList<>();
                stats.add(new ExplorationSelectionResultPayload.PlayerStats(owner.getUUID(),
                    result.ownerStats().rank(), result.ownerStats().power()));
                for (int index = 0; index < teammateIds.size() && index < result.memberStats().size(); index++) {
                    var member = result.memberStats().get(index);
                    stats.add(new ExplorationSelectionResultPayload.PlayerStats(teammateIds.get(index),
                        member.rank(), member.power()));
                }
                return stats;
                }

    public Type<ExplorationSelectionPayload> type() {
        return TYPE;
    }
}