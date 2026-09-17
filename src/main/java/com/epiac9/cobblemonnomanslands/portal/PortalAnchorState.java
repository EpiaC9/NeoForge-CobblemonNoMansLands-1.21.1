package com.epiac9.cobblemonnomanslands.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class PortalAnchorState {
    private final BlockPos center;
    private final Direction facing;
    private boolean active;
    private String instanceId;
    //calling portal state id

    public PortalAnchorState(BlockPos center, Direction facing) {
        this.center = center;
        this.facing = facing;
        this.active = false;
        this.instanceId = null;
    }

    public BlockPos getCenter() {
        return center;
    }
    public Direction getFacing() {
        return facing;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public String getInstanceId() {
        return instanceId;
    }
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

}
