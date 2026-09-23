package com.epiac9.cobblemonnomanslands.structure;

import com.epiac9.cobblemonnomanslands.marker.base.BaseMarkerBlock;
import com.epiac9.cobblemonnomanslands.portal.DungeonPortalBlock;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import com.epiac9.cobblemonnomanslands.structure.connection.RoomConnection;
import com.epiac9.cobblemonnomanslands.marker.room.RoomMarkerBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public final class WorldSpawnStructureState extends SavedData {
    private static final String PLACED_KEY = "placed";
    private static final String BOARD_POSITIONS_KEY = "board_positions";
    private static final String PORTAL_POSITIONS_KEY = "portal_positions";

    private boolean placed;
    private List<BlockPos> boardPositions = List.of();
    private List<BlockPos> portalPositions = List.of();

    private WorldSpawnStructureState() {
    }

    private WorldSpawnStructureState(CompoundTag tag) {
        placed = tag.getBoolean(PLACED_KEY);
        boardPositions = positionsFromTag(tag.getLongArray(BOARD_POSITIONS_KEY));
        portalPositions = positionsFromTag(tag.getLongArray(PORTAL_POSITIONS_KEY));
    }

    public static Factory<WorldSpawnStructureState> factory() {
        return new Factory<>(
                WorldSpawnStructureState::new,
            (tag, provider) -> new WorldSpawnStructureState(tag),
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
        for (BlockPos position : portalPositions) {
            PortalAnchorState portal = new PortalAnchorState(position);
            if (level.getBlockState(position).getBlock() instanceof DungeonPortalBlock
                    && level.getBlockState(position).getValue(DungeonPortalBlock.ACTIVE)) {
                portal.setActive(true);
            }
            portals.add(portal);
        }
        return new RoomConnection(roomData, portals);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean(PLACED_KEY, placed);
        tag.putLongArray(BOARD_POSITIONS_KEY, positionsToTag(boardPositions));
        tag.putLongArray(PORTAL_POSITIONS_KEY, positionsToTag(portalPositions));
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