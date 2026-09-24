package com.epiac9.cobblemonnomanslands.network;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.portal.runtime.PortalRuntimeEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExplorationCancelPayload(BlockPos boardPosition) implements CustomPacketPayload {
    public static final Type<ExplorationCancelPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(CobblemonNoMansLands.MODID, "exploration_cancel")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplorationCancelPayload> STREAM_CODEC =
        StreamCodec.of(
            (buffer, payload) -> BlockPos.STREAM_CODEC.encode(buffer, payload.boardPosition()),
            buffer -> new ExplorationCancelPayload(BlockPos.STREAM_CODEC.decode(buffer))
        );

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer owner) {
                PortalRuntimeEvents.getRuntime().getExplorationSelectionService()
                    .cancel(owner, boardPosition);
            }
        });
    }

    @Override
    public Type<ExplorationCancelPayload> type() {
        return TYPE;
    }
}
