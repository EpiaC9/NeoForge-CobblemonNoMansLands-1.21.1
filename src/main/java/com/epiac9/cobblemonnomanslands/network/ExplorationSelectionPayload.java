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
import java.util.List;

public record ExplorationSelectionPayload(BlockPos boardPosition, ResourceLocation explorationId)
    implements CustomPacketPayload {
    public static final Type<ExplorationSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonNoMansLands.MODID, "exploration_selection")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplorationSelectionPayload> STREAM_CODEC =
            StreamCodec.of(ExplorationSelectionPayload::write, ExplorationSelectionPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, ExplorationSelectionPayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.boardPosition());
        ResourceLocation.STREAM_CODEC.encode(buffer, payload.explorationId());
    }

    private static ExplorationSelectionPayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos boardPosition = BlockPos.STREAM_CODEC.decode(buffer);
        ResourceLocation explorationId = ResourceLocation.STREAM_CODEC.decode(buffer);
        return new ExplorationSelectionPayload(boardPosition, explorationId);
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
                    activePortalState
            );
                PacketDistributor.sendToPlayer(player, buildResult(boardPosition, explorationId, result));
        });
    }

    public static ExplorationSelectionResultPayload buildResult(BlockPos boardPosition,
                                                                 ResourceLocation explorationId,
                                                                 ExplorationSelectionService.SelectionResult result) {
        return new ExplorationSelectionResultPayload(
                    boardPosition,
                    explorationId,
                    result.accepted(),
                        result.reason(),
                        result.currentPower(),
                        result.requiredPower(),
                        result.ownerStats().partyCount(),
                        result.maximumPokemon(),
                        result.durationMinutes(),
                        result.startedAt(),
                        result.expiresAt()
        );
    }

    public Type<ExplorationSelectionPayload> type() {
        return TYPE;
    }
}