package com.epiac9.cobblemonnomanslands.portal;

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
        for (var postion : PortalGeometry.getInteriorPositions(anchor)) {
            level.setBlock(postion, inactivePortalState, 3);
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
        for (var postion : PortalGeometry.getInteriorPositions(anchor)) {
            level.setBlock(postion, activePortalState, 3);
        }
        anchor.setActive(true);
    } //set portal to active
}
