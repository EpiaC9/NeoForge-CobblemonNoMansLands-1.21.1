package com.epiac9.cobblemonnomanslands.portal.anchor;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PortalGeometry {
    private PortalGeometry() {
    }
    public record PortalCell(BlockPos position, int cell) {
    }

    public static List<PortalCell> getInteriorPositions(PortalAnchorState anchor) {
        BlockPos center = anchor.getCenter();
        List<PortalCell> position = new ArrayList<>(9);
        int cell = 0;

        for (int zOffset = -1; zOffset <= 1; zOffset++) {
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                position.add(new PortalCell(
                        new BlockPos(
                                center.getX() + xOffset,
                                center.getY(),
                                center.getZ() + zOffset
                        ), cell
                ));
                cell++;
            }
        } //calculating portal position
        return position;
    }

}
