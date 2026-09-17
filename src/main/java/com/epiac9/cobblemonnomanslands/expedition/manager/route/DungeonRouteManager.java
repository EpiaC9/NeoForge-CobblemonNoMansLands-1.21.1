package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.epiac9.cobblemonnomanslands.expedition.manager.DungeonLotManager;

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
        }
        if (request.getOwnerID() == null) {
            return new DungeonRouteResult(false,null, request.getDungeonKey(), "Owner ID was null.", null, null);
        }
        if (request.getDungeonKey() == null || request.getDungeonKey().isBlank()) {
            return new DungeonRouteResult(false,null, null, "Dungeon Key was null.", null, null);
        }
        if (request.getMode() == null) {
            return new DungeonRouteResult(false,null, null, "Mode was null.", null, null);
        }
        if (!validDungeon.contains(request.getDungeonKey())) {
            return new DungeonRouteResult(false,null, request.getDungeonKey(), "Invalid Dungeon Key.", null, null);
        }
        if (request.getDifficulty() < 1) {
            return new DungeonRouteResult(false,null, null, "Difficulty was negative.", null, null);
        }
        if (request.getTimerSeconds() <= 0) {
            return new DungeonRouteResult(false,null, null, "TimerSeconds was negative.", null, null);
        }
        if (lotManager == null) {
            return new DungeonRouteResult(false,null, null, "Lot Manager was null.", null, null);
        }
        if (instanceManager == null) {
            return new DungeonRouteResult(false,null, null, "Instance Manager was null.", null, null);
        }
        String pendingInstanceId = UUID.randomUUID().toString();
        Integer lot = lotManager.reserveLot(pendingInstanceId);
        if (lot == null) {
            return new DungeonRouteResult(false,null, null, "Lot was null.", null, null);
        }
        String routeId = UUID.randomUUID().toString();
        instanceManager.createInstance(pendingInstanceId, lot, request.getDungeonKey(), request.getOwnerID());
        return new DungeonRouteResult(
                true,
                routeId,
                request.getDungeonKey(),
                "Route accepted",
                pendingInstanceId,
                lot
        );
    }
}

