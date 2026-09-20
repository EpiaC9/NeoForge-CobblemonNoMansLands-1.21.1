package com.epiac9.cobblemonnomanslands.dimension.generation;

import com.epiac9.cobblemonnomanslands.dimension.DungeonBounds;
import com.epiac9.cobblemonnomanslands.dungeon.lot.DungeonLot;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public final class DungeonGenerationJob {
    private final String instanceId;
    private final UUID ownerId;
    private final DungeonLot lot;
    private final DungeonBounds bounds;

    private DungeonGenerationStatus status;
    private int nextChunkX;
    private int nextChunkZ;

    public DungeonGenerationJob(String instanceId, UUID ownerId, DungeonLot lot, DungeonBounds bounds) {
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("instanceId cannot be null or blank");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId cannot be null");
        }
        if (lot == null) {
            throw new IllegalArgumentException("lot cannot be null");
        }
        if (bounds == null) {
            throw new IllegalArgumentException("bounds cannot be null");
        }
        if (!bounds.fitsInside(lot)) {
            throw new IllegalArgumentException("bounds must be inside of lot");
        }

        this.instanceId = instanceId;
        this.ownerId = ownerId;
        this.lot = lot;
        this.bounds = bounds;
        this.status = DungeonGenerationStatus.PENDING;
        this.nextChunkX = bounds.minChunkX();
        this.nextChunkZ = bounds.minChunkZ();
    }

    public String instanceId() {
        return instanceId;
    }
    public UUID ownerId() {
        return ownerId;
    }
    public DungeonLot lot() {
        return lot;
    }
    public DungeonBounds bounds() {
        return bounds;
    }
    public DungeonGenerationStatus status() {
        return status;
    }

    public void setStatus(DungeonGenerationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        this.status = status;
    }

    public boolean hasMoreChunks() {
        return nextChunkZ <= bounds.maxChunkZ();
    }

    public BlockPos nextChunkOrigin() {
        if (!hasMoreChunks()) {
            return null;
        }

        return new BlockPos(nextChunkX << 4, bounds.origin().getY(), nextChunkZ << 4);
    }

    public void advanceChunk() {
        if (!hasMoreChunks()) {
            return;
        }
        nextChunkX++;

        if (nextChunkX > bounds.maxChunkX()) {
            nextChunkX = bounds.minChunkX();
            nextChunkZ++;
        }
    }
}
