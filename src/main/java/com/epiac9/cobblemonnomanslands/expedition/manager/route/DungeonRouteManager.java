package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.epiac9.cobblemonnomanslands.dungeon.lot.DungeonLotManager;

import java.util.Set;
import java.util.UUID;
import com.epiac9.cobblemonnomanslands.expedition.manager.instance.DungeonInstanceManager;

public class DungeonRouteManager {
    private final Set<String> validDungeon;

    public DungeonRouteManager(Set<String> validDungeon) {
        this.validDungeon = validDungeon;
    }

    public DungeonRouteResult route(DungeonRouteRequest request, DungeonLotManager lotManager, DungeonInstanceManager instanceManager) {
        if (request == null) {
            return new DungeonRouteResult(false,null, null, "Route request was null.", null, null);
        } //validate request
        if (request.getOwnerID() == null) {
            return new DungeonRouteResult(false,null, request.getDungeonKey(), "Owner ID was null.", null, null);
        } //validate owner
        if (request.getDungeonKey() == null || request.getDungeonKey().isBlank()) {
            return new DungeonRouteResult(false,null, null, "Dungeon Key was null.", null, null);
        } //validate dungeon key
        if (request.getMode() == null) {
            return new DungeonRouteResult(false,null, null, "Mode was null.", null, null);
        } //validate mode
        if (!validDungeon.contains(request.getDungeonKey())) {
            return new DungeonRouteResult(false,null, request.getDungeonKey(), "Invalid Dungeon Key.", null, null);
        } //dungeon key is not valid
        if (request.getDifficulty() < 1) {
            return new DungeonRouteResult(false,null, null, "Difficulty was negative.", null, null);
        } //validate difficulty
        if (request.getTimerSeconds() <= 0) {
            return new DungeonRouteResult(false,null, null, "TimerSeconds was negative.", null, null);
        } //validate timer
        if (lotManager == null) {
            return new DungeonRouteResult(false,null, null, "Lot Manager was null.", null, null);
        } //validate existing lot
        if (instanceManager == null) {
            return new DungeonRouteResult(false,null, null, "Instance Manager was null.", null, null);
        } //validate instance
        if (instanceManager.hasOccupiedInstance()) {
            return new DungeonRouteResult(false,null, request.getDungeonKey(), "Instance already exists.", null, null);
        } //validate existing instance
        String pendingInstanceId = UUID.randomUUID().toString();
        Integer lot = lotManager.reserveLot(pendingInstanceId);
        if (lot == null) {
            return new DungeonRouteResult(false,null, null, "Lot was null.", null, null);
        } //lot is full or can't be found
        String routeId = UUID.randomUUID().toString();
        instanceManager.createInstance(pendingInstanceId, lot, request.getDungeonKey(), request.getOwnerID());
        return new DungeonRouteResult(
                true,
                routeId,
                request.getDungeonKey(),
                "Route accepted",
                pendingInstanceId,
                lot
        ); //result
    }
}

