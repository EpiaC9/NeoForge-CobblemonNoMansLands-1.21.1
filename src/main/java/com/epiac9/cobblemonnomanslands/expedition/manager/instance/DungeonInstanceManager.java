package com.epiac9.cobblemonnomanslands.expedition.manager.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.epiac9.cobblemonnomanslands.expedition.manager.DungeonLotManager;

public class DungeonInstanceManager {
    private final Map<String, DungeonInstanceState> instances;
    private final DungeonLotManager lotManager;

    public DungeonInstanceManager(DungeonLotManager lotManager) {
        this.instances = new HashMap<>();
        this.lotManager = lotManager;
    }

    public DungeonInstanceState createInstance(String instanceId, int lotId, String dungeonKey, UUID ownerId) {
        DungeonInstanceState instance = new DungeonInstanceState(instanceId, lotId, dungeonKey, ownerId, DungeonInstanceState.InstanceStatus.PENDING);
        instances.put(instanceId, instance);
        return instance;
    }

    public DungeonInstanceState getInstance(String instanceId) {
        return instances.get(instanceId);
    }

    public boolean removeInstance(String instanceId) {
        DungeonInstanceState instance = instances.remove(instanceId);
        if (instance == null) {
            return false;
        }
        lotManager.releaseLot(instance.getInstanceId());
        return true;
    }
}
