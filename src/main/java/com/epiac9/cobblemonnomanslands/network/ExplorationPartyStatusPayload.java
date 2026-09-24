package com.epiac9.cobblemonnomanslands.network;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.client.ExplorationPartyClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExplorationPartyStatusPayload(BlockPos boardPosition, int currentPokemon, int maximumPokemon,
                                             int currentPower)
        implements CustomPacketPayload {
    public static final Type<ExplorationPartyStatusPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(CobblemonNoMansLands.MODID, "exploration_party_status")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplorationPartyStatusPayload> STREAM_CODEC =
        StreamCodec.of(
            (buffer, payload) -> {
                BlockPos.STREAM_CODEC.encode(buffer, payload.boardPosition());
                buffer.writeVarInt(payload.currentPokemon());
                buffer.writeVarInt(payload.maximumPokemon());
                buffer.writeVarInt(payload.currentPower());
            },
            buffer -> new ExplorationPartyStatusPayload(BlockPos.STREAM_CODEC.decode(buffer),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt())
        );

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ExplorationPartyClientState.accept(this));
    }

    @Override
    public Type<ExplorationPartyStatusPayload> type() {
        return TYPE;
    }
}
