package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import java.util.UUID;

public class DungeonRouteRequest {
    private final UUID ownerID;
    private final String dungeonKey;
    private final int difficulty;
    private final int timerSeconds;
    private final RouteMode mode;
    private final int requiredPower;
    private final int minPartySize;
    private final int maxPartySize;
    //hold route info

    public DungeonRouteRequest(UUID ownerID, String dungeonKey, int difficulty, int timerSeconds,
                               RouteMode mode, int requiredPower, int minPartySize, int maxPartySize) {
        this.ownerID = ownerID;
        this.dungeonKey = dungeonKey;
        this.difficulty = difficulty;
        this.timerSeconds = timerSeconds;
        this.mode = mode;
        this.requiredPower = requiredPower;
        this.minPartySize = minPartySize;
        this.maxPartySize = maxPartySize;
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
    public RouteMode getMode() {
        return mode;
    }
    public int getRequiredPower() {
        return requiredPower;
    }
    public int getMinPartySize() {
        return minPartySize;
    }
    public int getMaxPartySize() {
        return maxPartySize;
    }

    public enum RouteMode {
        PLAYER_ENTRY,
        EXPEDITION_DISPATCH
    }
}
