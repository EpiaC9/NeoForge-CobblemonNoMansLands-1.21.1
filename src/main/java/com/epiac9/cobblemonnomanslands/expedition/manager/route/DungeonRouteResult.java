package com.epiac9.cobblemonnomanslands.expedition.manager.route;

public class DungeonRouteResult {
    private final boolean accepted;
    private final String routeId;
    private final String dungeonKey;
    private final String reason;
    private final String pendingInstanceId;
    private final Integer lotId;

    public DungeonRouteResult(boolean accepted, String routeId, String dungeonKey, String reason, String pendingInstanceId, Integer lotId) {
        this.accepted = accepted;
        this.routeId = routeId;
        this.dungeonKey = dungeonKey;
        this.reason = reason;
        this.pendingInstanceId = pendingInstanceId;
        this.lotId = lotId;
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
    public String getPendingInstanceId() {
        return pendingInstanceId;
    }
    public Integer getLotId() {
        return lotId;
    }
}
