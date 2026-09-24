package com.epiac9.cobblemonnomanslands.portal.anchor;

import com.epiac9.cobblemonnomanslands.portal.DungeonPortalBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class PortalService {
    /** Reservations are runtime-only: recover saved portal cells without replacing other blocks. */
    public void recoverInactivePortal(LevelAccessor level, PortalAnchorState anchor) {
        for (PortalGeometry.PortalCell cell : PortalGeometry.getInteriorPositions(anchor)) {
            BlockState state = level.getBlockState(cell.position());
            if (state.getBlock() instanceof DungeonPortalBlock && state.getValue(DungeonPortalBlock.ACTIVE)) {
                level.setBlock(cell.position(), state.setValue(DungeonPortalBlock.ACTIVE, false), Block.UPDATE_ALL);
            }
        }
        anchor.setInstanceId(null);
        anchor.setActive(false);
    }

    public void createInactivePortal(ServerLevel level, PortalAnchorState anchor, BlockState inactivePortalState) {
        updatePortal(level, anchor, inactivePortalState, false);
    }

    public void activatePortal(ServerLevel level, PortalAnchorState anchor, BlockState activePortalState) {
        updatePortal(level, anchor, activePortalState, true);
    }

    public void deactivatePortal(ServerLevel level, PortalAnchorState anchor, BlockState inactivePortalState) {
        updatePortal(level, anchor, inactivePortalState, false);
    }

    private void updatePortal(ServerLevel level, PortalAnchorState anchor, BlockState portalState, boolean active) {
        Objects.requireNonNull(level, "Level cannot be null");
        Objects.requireNonNull(anchor, "Anchor cannot be null");
        Objects.requireNonNull(portalState, "PortalState cannot be null");

        for (PortalGeometry.PortalCell portalCell : PortalGeometry.getInteriorPositions(anchor)) {
            level.setBlock(portalCell.position(), portalState
                    .setValue(DungeonPortalBlock.CELL, portalCell.cell())
                    .setValue(DungeonPortalBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
        anchor.setActive(active);
    }
}
