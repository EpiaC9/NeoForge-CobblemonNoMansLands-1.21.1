package com.epiac9.cobblemonnomanslands.expedition.manager.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DungeonInstanceManager {
    private final Map<String, DungeonInstanceState> instances;

    public DungeonInstanceManager() {
        this.instances = new HashMap<>();
    }

    public DungeonInstanceState createInstance(String instanceId, String dimensionKey, UUID ownerId) {
        DungeonInstanceState instance = new DungeonInstanceState(instanceId, dimensionKey, ownerId, DungeonInstanceState.InstanceStatus.PENDING);
        instances.put(instanceId, instance);
        return instance;
    } //create new instance with id

    public DungeonInstanceState getInstance(String instanceId) {
        return instances.get(instanceId);
    }

    public boolean removeInstance(String instanceId) {
        DungeonInstanceState instance = instances.remove(instanceId);
        if (instance == null) {
            return false;
        }
        return true;
    } //remove instance

    public boolean hasInstanceOwnedBy(UUID ownerId) {
        return instances.values().stream().anyMatch(instance -> instance.getOwnerId().equals(ownerId));
    }
}
