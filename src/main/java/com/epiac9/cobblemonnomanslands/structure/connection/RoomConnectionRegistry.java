package com.epiac9.cobblemonnomanslands.structure.connection;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class RoomConnectionRegistry {
    private final Map<ResourceKey<Level>, Map<BlockPos, RoomConnection>> connectionsByBoard;

    public RoomConnectionRegistry() {
        this.connectionsByBoard = new HashMap<>();
    }

    public void register(ResourceKey<Level> dimension, RoomConnection connection) {
        if (dimension == null) {
            throw new IllegalArgumentException("Dimension cannot be null");
        }
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        Map<BlockPos, RoomConnection> connections = connectionsByBoard.computeIfAbsent(
                dimension, ignored -> new HashMap<>()
        );

        for (BlockPos boardPosition : connection.boardPositions()) {
            connections.put(boardPosition.immutable(), connection);
        }
    }

    public RoomConnection findByBoard(ResourceKey<Level> dimension, BlockPos boardPosition) {
        if (dimension == null) {
            return null;
        }

        Map<BlockPos, RoomConnection> connections = connectionsByBoard.get(dimension);
        if (connections == null) {
            return null;
        }

        return connections.get(boardPosition);
    }

    public boolean unregister(ResourceKey<Level> dimension, RoomConnection connection) {
        if (dimension == null || connection == null) {
            return false;
        }

        Map<BlockPos, RoomConnection> connections = connectionsByBoard.get(dimension);
        if (connections == null) {
            return false;
        }

        boolean removed = false;
        for (BlockPos boardPosition : connection.boardPositions()) {
            if (connections.remove(boardPosition, connection)) {
                removed = true;
            }
        }

        if (connections.isEmpty()) {
            connectionsByBoard.remove(dimension);
        }

        return removed;
    }

    public void clear() {
        connectionsByBoard.clear();
    }
}
