package com.epiac9.cobblemonnomanslands.network;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.client.ExplorationPartyClientState;
import com.epiac9.cobblemonnomanslands.expedition.manager.route.ExpeditionRouteService;
import com.epiac9.cobblemonnomanslands.expedition.selection.ExplorationSelectionState;
import com.epiac9.cobblemonnomanslands.expedition.stats.ExplorationPlayerStats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExplorationPartyStatusPayload(ResourceLocation dimensionId, BlockPos boardPosition,
                                             boolean boardAccessible, boolean statsAvailable, int currentRank,
                                             int currentPokemon, int maximumPokemon, int currentPower,
                                             boolean portalAvailable, boolean ownerHasPending,
                                             ResourceLocation activeExplorationId)
        implements CustomPacketPayload {
    public static final Type<ExplorationPartyStatusPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(CobblemonNoMansLands.MODID, "exploration_party_status")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExplorationPartyStatusPayload> STREAM_CODEC =
        StreamCodec.of(
            (buffer, payload) -> {
                ResourceLocation.STREAM_CODEC.encode(buffer, payload.dimensionId());
                BlockPos.STREAM_CODEC.encode(buffer, payload.boardPosition());
                buffer.writeBoolean(payload.boardAccessible());
                buffer.writeBoolean(payload.statsAvailable());
                buffer.writeVarInt(payload.currentRank());
                buffer.writeVarInt(payload.currentPokemon());
                buffer.writeVarInt(payload.maximumPokemon());
                buffer.writeVarInt(payload.currentPower());
                buffer.writeBoolean(payload.portalAvailable());
                buffer.writeBoolean(payload.ownerHasPending());
                buffer.writeBoolean(payload.activeExplorationId() != null);
                if (payload.activeExplorationId() != null) {
                    ResourceLocation.STREAM_CODEC.encode(buffer, payload.activeExplorationId());
                }
            },
            buffer -> new ExplorationPartyStatusPayload(ResourceLocation.STREAM_CODEC.decode(buffer),
                BlockPos.STREAM_CODEC.decode(buffer), buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean() ? ResourceLocation.STREAM_CODEC.decode(buffer) : null)
        );

    public static ExplorationPartyStatusPayload snapshot(ResourceLocation dimension, BlockPos board,
                                                          ExplorationPlayerStats stats, boolean portalAvailable,
                                                          ExplorationSelectionState pending) {
        ResourceLocation activeId = pending != null && pending.dimension().location().equals(dimension)
            && pending.boardPosition().equals(board) ? pending.explorationId() : null;
        return new ExplorationPartyStatusPayload(dimension, board, true, stats.available(), stats.rank(), stats.partyCount(),
            ExpeditionRouteService.maximumExplorationPokemon(stats.rank()), stats.power(),
            portalAvailable, pending != null, activeId);
    }

    public static ExplorationPartyStatusPayload unavailable(ResourceLocation dimension, BlockPos board) {
        return new ExplorationPartyStatusPayload(dimension, board, false, false, 0, 0, 1, 0, false, false, null);
    }

    public boolean canStart() {
        return boardAccessible && statsAvailable && portalAvailable && !ownerHasPending;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ExplorationPartyClientState.accept(this));
    }

    @Override
    public Type<ExplorationPartyStatusPayload> type() {
        return TYPE;
    }
}
