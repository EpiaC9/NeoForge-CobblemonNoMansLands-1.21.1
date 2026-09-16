package com.epiac9.cobblemonnomanslands.expedition;

public class DungeonRouteResult {
    private final boolean accepted;
    private final String routeId;
    private final String dungeonKey;
    private final String reason;

    public DungeonRouteResult(boolean accepted, String routeId, String dungeonKey, String reason) {
        this.accepted = accepted;
        this.routeId = routeId;
        this.dungeonKey = dungeonKey;
        this.reason = reason;
    }

    public boolean isAccepted() {
        return accepted;
    }
    public String getRouteId() {
        return routeId;
    }
    public String getDungeonKey() {
        return dungeonKey;
    }
    public String getReason() {
        return reason;
    }
}
