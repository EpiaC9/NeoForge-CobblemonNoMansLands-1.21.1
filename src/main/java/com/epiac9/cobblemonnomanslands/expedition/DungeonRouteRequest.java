package com.epiac9.cobblemonnomanslands.expedition;

import java.util.UUID;

public class DungeonRouteRequest {
    private final UUID ownerID;
    private final String dungeonKey;
    private final int difficulty;
    private final int timerSeconds;
    private final RouteMode mode;

    public DungeonRouteRequest(UUID ownerID, String dungeonKey, int difficulty, int timerSeconds, RouteMode mode) {
        this.ownerID = ownerID;
        this.dungeonKey = dungeonKey;
        this.difficulty = difficulty;
        this.timerSeconds = timerSeconds;
        this.mode = mode;
    }

    public UUID getOwnerID() {
        return ownerID;
    }
    public String getDungeonKey() {
        return dungeonKey;
    }
    public int getDifficulty() {
        return difficulty;
    }
    public int getTimerSeconds() {
        return timerSeconds;
    }

    public enum RouteMode {
        PLAYER_ENTRY,
        EXPEDITION_DISPATCH
    }
}
