package com.epiac9.cobblemonnomanslands.expedition.manager.instance;

import java.util.UUID;

public class DungeonInstanceState {
    private final String instanceId;
    private final String dimensionKey;
    private final UUID ownerId;
    private InstanceStatus status;
    //get instance id

    public DungeonInstanceState(String instanceId, String dimensionKey, UUID ownerId, InstanceStatus status) {
        this.instanceId = instanceId;
        this.dimensionKey = dimensionKey;
        this.ownerId = ownerId;
        this.status = status;
    }

    public String getInstanceId() {
        return instanceId;
    }
    public String getDimensionKey() {
        return dimensionKey;
    }
    public UUID getOwnerId() {
        return ownerId;
    }
    public InstanceStatus getStatus() {
        return status;
    }

    public void setStatus(InstanceStatus status) {
        this.status = status;
    }

    public enum InstanceStatus {
        PENDING,
        ACTIVE,
        COMPLETED,
        RESETTING
    } //marking status of instance
}
