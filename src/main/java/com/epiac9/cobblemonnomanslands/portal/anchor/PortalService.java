package com.epiac9.cobblemonnomanslands.portal.anchor;

import com.epiac9.cobblemonnomanslands.portal.DungeonPortalBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class PortalService {
    public void createInactivePortal(ServerLevel level, PortalAnchorState anchor, BlockState inactivePortalState) {
        if (level == null) {
            throw new NullPointerException("Level cannot be null");
        }
        if (anchor == null) {
            throw new NullPointerException("Anchor cannot be null");
        }
        if (inactivePortalState == null) {
            throw new NullPointerException("PortalState cannot be null");
        }
        for (PortalGeometry.PortalCell portalCell : PortalGeometry.getInteriorPositions(anchor)) {
            level.setBlock(portalCell.position(), inactivePortalState
                    .setValue(DungeonPortalBlock.CELL, portalCell.cell())
                    .setValue(DungeonPortalBlock.ACTIVE, false), 3
            );
        }
        anchor.setActive(false);
    } //validate authority, anchor and state of portal before placement. Portal must be inactive on place.

    public void activatePortal(ServerLevel level, PortalAnchorState anchor, BlockState activePortalState) {
        if (level == null) {
            throw new NullPointerException("Level cannot be null");
        }
        if (anchor == null) {
            throw new NullPointerException("Anchor cannot be null");
        }
        if (activePortalState == null) {
            throw new NullPointerException("PortalState cannot be null");
        }
        for (PortalGeometry.PortalCell portalCell : PortalGeometry.getInteriorPositions(anchor)) {
            level.setBlock(portalCell.position(), activePortalState
                    .setValue(DungeonPortalBlock.CELL, portalCell.cell())
                    .setValue(DungeonPortalBlock.ACTIVE, true), 3
            );
        }
        anchor.setActive(true);
    } //set portal to active
}
