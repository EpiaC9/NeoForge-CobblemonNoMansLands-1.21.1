package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import java.util.UUID;

public class DungeonRouteRequest {
    private final UUID ownerID;
    private final String dimensionKey;
    private final int difficulty;
    private final int timerSeconds;
    private final RouteMode mode;

    public DungeonRouteRequest(UUID ownerID, String dimensionKey, int difficulty, int timerSeconds,
                               RouteMode mode) {
        this.ownerID = ownerID;
        this.dimensionKey = dimensionKey;
        this.difficulty = difficulty;
        this.timerSeconds = timerSeconds;
        this.mode = mode;
    }

    public UUID getOwnerID() {
        return ownerID;
    }
    public String getDimensionKey() {
        return dimensionKey;
    }
    public int getDifficulty() {
        return difficulty;
    }
    public int getTimerSeconds() {
        return timerSeconds;
    }
    public RouteMode getMode() {
        return mode;
    }

    public enum RouteMode {
        PLAYER_ENTRY,
        EXPEDITION_DISPATCH
    }
}
