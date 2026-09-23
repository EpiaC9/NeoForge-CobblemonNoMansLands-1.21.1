package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import java.util.Set;
import java.util.UUID;
import com.epiac9.cobblemonnomanslands.expedition.manager.instance.DungeonInstanceManager;

public class DungeonRouteManager {
    private final Set<String> validDimensions;

    public DungeonRouteManager(Set<String> validDimensions) {
        this.validDimensions = validDimensions;
    }

    public DungeonRouteResult route(DungeonRouteRequest request, DungeonInstanceManager instanceManager) {
        if (request == null) {
            return DungeonRouteResult.rejected("Route request was null.");
        } //validate request
        if (request.getOwnerID() == null) {
            return DungeonRouteResult.rejected(request.getDimensionKey(), "Owner ID was null.");
        } //validate owner
        if (request.getDimensionKey() == null || request.getDimensionKey().isBlank()) {
            return DungeonRouteResult.rejected("Dimension Key was null.");
        } //validate dungeon key
        if (request.getMode() == null) {
            return DungeonRouteResult.rejected("Mode was null.");
        } //validate mode
        if (!validDimensions.contains(request.getDimensionKey())) {
            return DungeonRouteResult.rejected(request.getDimensionKey(), "Invalid Dimension Key.");
        } //dimension key is not valid
        if (request.getDifficulty() < 1) {
            return DungeonRouteResult.rejected("Difficulty was negative.");
        } //validate difficulty
        if (request.getTimerSeconds() <= 0) {
            return DungeonRouteResult.rejected("TimerSeconds was negative.");
        } //validate timer
        if (instanceManager == null) {
            return DungeonRouteResult.rejected("Instance Manager was null.");
        } //validate instance
        if (instanceManager.hasOccupiedInstance()) {
            return DungeonRouteResult.rejected(request.getDimensionKey(), "Instance already exists.");
        } //validate existing instance
        String pendingInstanceId = UUID.randomUUID().toString();
        String routeId = UUID.randomUUID().toString();
        instanceManager.createInstance(pendingInstanceId, request.getDimensionKey(), request.getOwnerID());
        return DungeonRouteResult.accepted(routeId, request.getDimensionKey(), pendingInstanceId);
    }
}

