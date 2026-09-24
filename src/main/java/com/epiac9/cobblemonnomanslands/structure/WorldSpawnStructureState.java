package com.epiac9.cobblemonnomanslands.structure;

import com.epiac9.cobblemonnomanslands.marker.base.BaseMarkerBlock;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalService;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnection;
import com.epiac9.cobblemonnomanslands.marker.room.RoomMarkerBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldSpawnStructureState extends SavedData {
    private static final String PLACED_KEY = "placed";
    private static final String BOARD_POSITIONS_KEY = "board_positions";
    private static final String PORTAL_POSITIONS_KEY = "portal_positions";
    private static final String PROTECTED_BLOCKS_KEY = "protected_blocks";

    private boolean placed;
    private List<BlockPos> boardPositions = List.of();
    private List<BlockPos> portalPositions = List.of();
    private Map<BlockPos, BlockState> protectedBlocks = Map.of();

    private WorldSpawnStructureState() {
    }

    private WorldSpawnStructureState(CompoundTag tag, HolderLookup.Provider provider) {
        placed = tag.getBoolean(PLACED_KEY);
        boardPositions = positionsFromTag(tag.getLongArray(BOARD_POSITIONS_KEY));
        portalPositions = positionsFromTag(tag.getLongArray(PORTAL_POSITIONS_KEY));
        Map<BlockPos, BlockState> restored = new HashMap<>();
        HolderLookup<Block> blocks = provider.lookupOrThrow(Registries.BLOCK);
        ListTag entries = tag.getList(PROTECTED_BLOCKS_KEY, 10);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            restored.put(BlockPos.of(entry.getLong("pos")), NbtUtils.readBlockState(blocks, entry));
        }
        protectedBlocks = Map.copyOf(restored);
    }

    public static Factory<WorldSpawnStructureState> factory() {
        return new Factory<>(
                WorldSpawnStructureState::new,
            (tag, provider) -> new WorldSpawnStructureState(tag, provider),
                DataFixTypes.LEVEL
        );
    }

    public boolean isPlaced() {
        return placed;
    }

    public void markPlaced() {
        if (!placed) {
            placed = true;
            setDirty();
        }
    }

    public void recordConnection(RoomConnection connection) {
        boardPositions = List.copyOf(connection.boardPositions());
        portalPositions = List.copyOf(connection.portalPositions());
        setDirty();
    }

    public void recordProtectedBlocks(Map<BlockPos, BlockState> blocks) {
        protectedBlocks = Map.copyOf(blocks);
        setDirty();
    }

    public Map<BlockPos, BlockState> protectedBlocks() {
        return protectedBlocks;
    }

    public RoomConnection restoreConnection(ServerLevel level) {
        if (boardPositions.isEmpty() || portalPositions.isEmpty()) {
            return null;
        }

        List<BaseMarkerBlock.MarkerData> markers = boardPositions.stream()
                .map(position -> new BaseMarkerBlock.MarkerData(position, "board"))
                .toList();
        RoomMarkerBlock.RoomData roomData = new RoomMarkerBlock.RoomData(
                BlockPos.ZERO,
                BlockPos.ZERO,
                BlockPos.ZERO,
                markers
        );
        List<PortalAnchorState> portals = new ArrayList<>();
        PortalService portalService = new PortalService();
        for (BlockPos position : portalPositions) {
            PortalAnchorState portal = new PortalAnchorState(position);
            portalService.recoverInactivePortal(level, portal);
            portals.add(portal);
        }
        return new RoomConnection(roomData, portals);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean(PLACED_KEY, placed);
        tag.putLongArray(BOARD_POSITIONS_KEY, positionsToTag(boardPositions));
        tag.putLongArray(PORTAL_POSITIONS_KEY, positionsToTag(portalPositions));
        ListTag entries = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : protectedBlocks.entrySet()) {
            CompoundTag block = NbtUtils.writeBlockState(entry.getValue());
            block.putLong("pos", entry.getKey().asLong());
            entries.add(block);
        }
        tag.put(PROTECTED_BLOCKS_KEY, entries);
        return tag;
    }

    private static long[] positionsToTag(List<BlockPos> positions) {
        return positions.stream().mapToLong(BlockPos::asLong).toArray();
    }

    private static List<BlockPos> positionsFromTag(long[] positions) {
        List<BlockPos> result = new ArrayList<>(positions.length);
        for (long position : positions) {
            result.add(BlockPos.of(position));
        }
        return List.copyOf(result);
    }
}
