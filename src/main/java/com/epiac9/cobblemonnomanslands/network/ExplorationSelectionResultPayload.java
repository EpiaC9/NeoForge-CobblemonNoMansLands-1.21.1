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
                                                int requiredPower, int currentMembers, int maxMembers,
                                                java.util.List<PlayerStats> playerStats)
        implements CustomPacketPayload {
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
        buffer.writeVarInt(payload.currentMembers());
        buffer.writeVarInt(payload.maxMembers());
        buffer.writeVarInt(payload.playerStats().size());
        for (PlayerStats stats : payload.playerStats()) {
            net.minecraft.core.UUIDUtil.STREAM_CODEC.encode(buffer, stats.playerId());
            buffer.writeVarInt(stats.rank());
            buffer.writeVarInt(stats.power());
        }
    }

    private static ExplorationSelectionResultPayload read(RegistryFriendlyByteBuf buffer) {
        BlockPos boardPosition = BlockPos.STREAM_CODEC.decode(buffer);
        ResourceLocation explorationId = ResourceLocation.STREAM_CODEC.decode(buffer);
        boolean accepted = buffer.readBoolean();
        String reason = buffer.readUtf(256);
        int currentPower = buffer.readVarInt();
        int requiredPower = buffer.readVarInt();
        int currentMembers = buffer.readVarInt();
        int maxMembers = buffer.readVarInt();
        int count = buffer.readVarInt();
        java.util.List<PlayerStats> playerStats = new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            playerStats.add(new PlayerStats(net.minecraft.core.UUIDUtil.STREAM_CODEC.decode(buffer),
                buffer.readVarInt(), buffer.readVarInt()));
        }
        return new ExplorationSelectionResultPayload(
            boardPosition, explorationId, accepted, reason,
            currentPower, requiredPower, currentMembers, maxMembers, playerStats
        );
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ExplorationSelectionClientState.accept(this));
    }

    @Override
    public Type<ExplorationSelectionResultPayload> type() {
        return TYPE;
    }

    public record PlayerStats(java.util.UUID playerId, int rank, int power) {
    }
}