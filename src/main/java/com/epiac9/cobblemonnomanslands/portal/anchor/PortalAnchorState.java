package com.epiac9.cobblemonnomanslands.portal.anchor;

import net.minecraft.core.BlockPos;

public class PortalAnchorState {
    private final BlockPos center;
    private boolean active;
    private String instanceId;
    //calling portal state id

    public PortalAnchorState(BlockPos center) {
        this.center = center;
        this.active = false;
        this.instanceId = null;
    }

    public BlockPos getCenter() {
        return center;
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
