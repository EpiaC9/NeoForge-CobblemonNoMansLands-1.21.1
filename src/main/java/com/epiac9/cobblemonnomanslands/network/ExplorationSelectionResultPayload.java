package com.epiac9.cobblemonnomanslands.network;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.client.ExplorationSelectionClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExplorationSelectionResultPayload(BlockPos boardPosition, ResourceLocation explorationId,
                                                boolean accepted, String reason, int currentPower,
                                                int requiredPower, int currentPokemon, int maximumPokemon,
                                                int durationMinutes,
                                                long startedAt, long expiresAt) implements CustomPacketPayload {
    public static final Type<ExplorationSelectionResultPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(CobblemonNoMansLands.MODID, "exploration_selection_result")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplorationSelectionResultPayload> STREAM_CODEC =
        StreamCodec.of(ExplorationSelectionResultPayload::write, ExplorationSelectionResultPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, ExplorationSelectionResultPayload payload) {
        BlockPos.STREAM_CODEC.encode(buffer, payload.boardPosition());
        ResourceLocation.STREAM_CODEC.encode(buffer, payload.explorationId());
        buffer.writeBoolean(payload.accepted());
        buffer.writeUtf(payload.reason(), 256);
        buffer.writeVarInt(payload.currentPower());
        buffer.writeVarInt(payload.requiredPower());
        buffer.writeVarInt(payload.currentPokemon());
        buffer.writeVarInt(payload.maximumPokemon());
        buffer.writeVarInt(payload.durationMinutes());
        buffer.writeLong(payload.startedAt());
        buffer.writeLong(payload.expiresAt());
    }

    private static ExplorationSelectionResultPayload read(RegistryFriendlyByteBuf buffer) {
        return new ExplorationSelectionResultPayload(
            BlockPos.STREAM_CODEC.decode(buffer),
            ResourceLocation.STREAM_CODEC.decode(buffer),
            buffer.readBoolean(),
            buffer.readUtf(256),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readLong(),
            buffer.readLong()
        );
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ExplorationSelectionClientState.accept(this));
    }

    @Override
    public Type<ExplorationSelectionResultPayload> type() {
        return TYPE;
    }
}
