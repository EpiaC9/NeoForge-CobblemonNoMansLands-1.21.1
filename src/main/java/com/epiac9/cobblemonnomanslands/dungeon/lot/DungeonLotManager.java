package com.epiac9.cobblemonnomanslands.dungeon.lot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DungeonLotManager {
    private final int lotCount;
    private final Set<Integer> availableLot;
    private final Map<String, Integer> pendingInstanceToLot;
    //tracking lot

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
    } //tracking available lot for dungeon generation

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
    } //marking lot for clean up

    public Integer getLotForInstance(String pendingInstanceId) {
        if (pendingInstanceId == null || pendingInstanceId.isBlank()) {
            return null;
        }
        return pendingInstanceToLot.get(pendingInstanceId);
    } //prepare lot for new instance

    public DungeonLot getDungeonLotForInstance(String pendingInstanceId) {
        Integer lotId = getLotForInstance(pendingInstanceId);
        if (lotId == null) {
            return null;
        }

        return DungeonLotRegistry.get(lotId);
    }

    public int getLotCount() {
        return this.lotCount;
    } //update lot count

    public boolean hasAvailableLot() {
        return !availableLot.isEmpty();
    } //mark lot as available
}
