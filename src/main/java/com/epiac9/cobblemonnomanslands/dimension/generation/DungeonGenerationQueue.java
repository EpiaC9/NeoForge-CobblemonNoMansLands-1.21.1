package com.epiac9.cobblemonnomanslands.dimension.generation;

import com.epiac9.cobblemonnomanslands.dimension.DungeonBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayDeque;
import java.util.Queue;

public final class DungeonGenerationQueue {
    private final Queue<DungeonGenerationJob> pendingJobs = new ArrayDeque<>();

    public void submit(DungeonGenerationJob job) {
        if (job == null) {
            throw new IllegalArgumentException("job cannot be null");
        }

        pendingJobs.add(job);
    }

    public void tick(ServerLevel level) {
        if (level == null || pendingJobs.isEmpty()) {
            return;
        }

        DungeonGenerationJob job = pendingJobs.peek();
        if (job.status() == DungeonGenerationStatus.PENDING) {
            job.setStatus(DungeonGenerationStatus.GENERATING);
        }
        processNextChunk(level, job);
        if (!job.hasMoreChunks()) {
            job.setStatus(DungeonGenerationStatus.ACTIVE);
            pendingJobs.remove();
        }
    }

    private void processNextChunk(ServerLevel level, DungeonGenerationJob job) {
        BlockPos chunkOrigin = job.nextChunkOrigin();
        if (chunkOrigin == null) {
            return;
        }
        DungeonBounds bounds = job.bounds();

        int chunkMinX = chunkOrigin.getX();
        int chunkMinZ = chunkOrigin.getZ();
        int chunkMaxX = chunkMinX + 15;
        int chunkMaxZ = chunkMinZ + 15;

        int minX = Math.max(chunkMinX, bounds.origin().getX());
        int maxX = Math.min(chunkMaxX, bounds.max().getX());
        int minZ = Math.max(chunkMinZ, bounds.origin().getZ());
        int maxZ = Math.min(chunkMaxZ, bounds.max().getZ());

        level.getChunkAt(chunkOrigin);
        for (int y = bounds.origin().getY(); y <= bounds.max().getY(); y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        job.advanceChunk();
    }
}
