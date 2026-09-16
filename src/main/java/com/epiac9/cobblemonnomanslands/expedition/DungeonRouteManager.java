package com.epiac9.cobblemonnomanslands.expedition;

import java.util.Set;
import java.util.UUID;

public class DungeonRouteManager {
    private final Set<String> validDungeon;

    public DungeonRouteManager(Set<String> validDungeon) {
        this.validDungeon = validDungeon;
    }

    public DungeonRouteResult route(DungeonRouteRequest request) {
        if (request == null) {
            return new DungeonRouteResult(false,null, null, "Route request was null.");
        }

        if (request.getOwnerID() == null) {
            return new DungeonRouteResult(false,null, request.getDungeonKey(), "Owner ID was null.");
        }

        if (request.getDungeonKey() == null || request.getDungeonKey().isBlank()) {
            return new DungeonRouteResult(false,null, null, "Dungeon Key was null.");
        }

        if (!validDungeon.contains(request.getDungeonKey())) {
            return new DungeonRouteResult(false,null, request.getDungeonKey(), "Invalid Dungeon Key.");
        }

        if (request.getDifficulty() < 1) {
            return new DungeonRouteResult(false,null, null, "Difficulty was negative.");
        }

        if (request.getTimerSeconds() <= 0) {
            return new DungeonRouteResult(false,null, null, "TimerSeconds was negative.");
        }

        String routeId = UUID.randomUUID().toString();
        return new DungeonRouteResult(true, routeId, request.getDungeonKey(), "Route accepted");
    }
}

