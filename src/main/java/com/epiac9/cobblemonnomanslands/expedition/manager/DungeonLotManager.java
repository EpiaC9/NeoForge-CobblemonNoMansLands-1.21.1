package com.epiac9.cobblemonnomanslands.expedition.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DungeonLotManager {
    private final int lotCount;
    private final Set<Integer> availableLot;
    private final Map<String, Integer> pendingInstanceToLot;

    public DungeonLotManager(int lotCount) {
        if (lotCount <= 0) {
            throw new IllegalArgumentException("lotCount must be greater than 0");
        }
        this.lotCount = lotCount;
        this.availableLot = new HashSet<>();
        this.pendingInstanceToLot = new HashMap<>();
        for (int i = 0; i < this.lotCount; i++) {
            this.availableLot.add(i);
        }
    }

    public int getLotCount() {
        return this.lotCount;
    }

    public boolean hasAvailableLot() {
        return !availableLot.isEmpty();
    }

    public Integer reserveLot(String pendingInstanceId) {
        if (pendingInstanceId == null ||  pendingInstanceId.isBlank()) {
            throw new IllegalArgumentException("pendingInstanceId cannot be null or blank");
        }
        if (pendingInstanceToLot.containsKey(pendingInstanceId)) {
            return pendingInstanceToLot.get(pendingInstanceId);
        }
        if (availableLot.isEmpty()) {
            return null;
        }
        int lot = availableLot.stream().findFirst().orElseThrow(() -> new IllegalStateException("No available lot for lot: " + pendingInstanceId));
        availableLot.remove(lot);
        pendingInstanceToLot.put(pendingInstanceId, lot);
        return lot;
    }

    public boolean releaseLot(String pendingInstanceId) {
        if (pendingInstanceId == null || pendingInstanceId.isBlank()) {
            return false;
        }
        Integer lot = pendingInstanceToLot.remove(pendingInstanceId);
        if (lot == null) {
            return false;
        }
        availableLot.add(lot);
        return true;
    }

    public Integer getLotforInstance(String pendingInstanceId) {
        if (pendingInstanceId == null || pendingInstanceId.isBlank()) {
            return null;
        }
        return pendingInstanceToLot.get(pendingInstanceId);
    }
}
