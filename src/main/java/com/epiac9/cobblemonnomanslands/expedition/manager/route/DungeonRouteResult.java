package com.epiac9.cobblemonnomanslands.expedition.manager.route;

public record DungeonRouteResult(boolean accepted, String routeId, String dimensionKey,
                                 String reason, String pendingInstanceId) {
    public static DungeonRouteResult accepted(String routeId, String dimensionKey, String pendingInstanceId) {
        return new DungeonRouteResult(true, routeId, dimensionKey, "Route accepted", pendingInstanceId);
    }

    public static DungeonRouteResult rejected(String reason) {
        return rejected(null, reason);
    }

    public static DungeonRouteResult rejected(String dimensionKey, String reason) {
        return new DungeonRouteResult(false, null, dimensionKey, reason, null);
    }
}
