package com.epiac9.cobblemonnomanslands.structure.connection;

import com.epiac9.cobblemonnomanslands.marker.room.RoomMarkerBlock;
import com.epiac9.cobblemonnomanslands.portal.anchor.PortalAnchorState;
import net.minecraft.core.BlockPos;

import java.util.List;

public record RoomConnection(RoomMarkerBlock.RoomData roomData, List<PortalAnchorState> portals) {
    public RoomConnection {
        if (roomData == null) {
            throw new IllegalStateException("Room connection has not been created");
        }

        portals = List.copyOf(portals);
    }

    public List<BlockPos> boardPositions() {
        return roomData.markers().stream().filter(marker -> marker.role().equals("board"))
                .map(RoomMarkerData -> RoomMarkerData.pos().immutable())
                .toList();
    }

    public List<BlockPos> portalPositions() {
        return portals.stream().map(PortalAnchorState::getCenter)
                .map(BlockPos::immutable)
                .toList();
    }

    public PortalAnchorState findPortal(BlockPos position) {
        if (position == null) {
            return null;
        }

        return portals.stream().filter(portal -> portal.getCenter().equals(position))
                .findFirst().orElse(null);
    }

    public PortalAnchorState findAvailablePortal() {
        return portals.stream().filter(portal -> !portal.isActive())
                .findFirst().orElse(null);
    }

    public boolean containsBoard(BlockPos position) {
        return boardPositions().contains(position);
    }

    public boolean containsPortal(BlockPos position) {
        return portalPositions().contains(position);
    }
}
