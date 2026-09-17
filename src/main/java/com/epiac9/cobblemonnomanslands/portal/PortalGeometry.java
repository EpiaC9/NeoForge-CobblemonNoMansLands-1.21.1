package com.epiac9.cobblemonnomanslands.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class PortalGeometry {
    private PortalGeometry() {
    }

    public static List<BlockPos> getInteriorPositions(PortalAnchorState anchor) {
        BlockPos center = anchor.getCenter();
        Direction facing = anchor.getFacing();
        List<BlockPos> interiorPositions = new ArrayList<>(9);

        for (int vertical = -1; vertical <= 1; vertical++) {
            for (int horizontal = -1; horizontal <= 1; horizontal++) {
                int xOffset = 0;
                int zOffset = 0;

                if (facing.getAxis() == Direction.Axis.X) {
                    zOffset = horizontal;
                } else {
                    xOffset = horizontal;
                }
                interiorPositions.add(new BlockPos(center.getX() + xOffset, center.getY() + vertical, center.getZ() + zOffset));
            }
        } //calculating portal position
        return interiorPositions;
    }

}
