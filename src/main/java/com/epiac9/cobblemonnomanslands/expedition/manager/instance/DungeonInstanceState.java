package com.epiac9.cobblemonnomanslands.expedition.manager.instance;

import java.util.UUID;

public class DungeonInstanceState {
    private final String instanceId;
    private final int lotId;
    private final String dungeonKey;
    private final UUID ownerId;
    private InstanceStatus status;
    //get instance id

    public DungeonInstanceState(String instanceId, int lotId, String dungeonKey, UUID ownerId, InstanceStatus status) {
        this.instanceId = instanceId;
        this.lotId = lotId;
        this.dungeonKey = dungeonKey;
        this.ownerId = ownerId;
        this.status = status;
    }

    public String getInstanceId() {
        return instanceId;
    }
    public int getLotId() {
        return lotId;
    }
    public String getDungeonKey() {
        return dungeonKey;
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
