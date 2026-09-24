package com.epiac9.cobblemonnomanslands.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;
import java.util.function.ToIntBiFunction;

/** Bounded, read-only search; an unsafe destination never consumes a reservation. */
public final class PortalArrivalResolver {
    private static final int SEARCH_RADIUS = 8;

    private PortalArrivalResolver() {}

    public static Vec3 find(ServerLevel level, ServerPlayer player) {
        return search(level.getSharedSpawnPos(),
            (x, z) -> level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z),
            pos -> {
                if (!level.getWorldBorder().isWithinBounds(pos)) return null;
                Vec3 candidate = safeAt(level, pos, level.getMinBuildHeight(), level.getMaxBuildHeight());
                if (candidate == null) return null;
                AABB bounds = player.getDimensions(Pose.STANDING).makeBoundingBox(candidate);
                return safeVolume(level, bounds, level.getMinBuildHeight(), level.getMaxBuildHeight())
                    && level.noCollision(player, bounds) ? candidate : null;
            });
    }

    static Vec3 search(BlockPos spawn, ToIntBiFunction<Integer, Integer> height,
                       Function<BlockPos, Vec3> safePosition) {
        Vec3 exact = safePosition.apply(spawn);
        if (exact != null) return exact;
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = spawn.getX() + dx, z = spawn.getZ() + dz;
                    Vec3 candidate = safePosition.apply(new BlockPos(x, height.applyAsInt(x, z), z));
                    if (candidate != null) return candidate;
                }
            }
        }
        return null;
    }

    static Vec3 safeAt(CollisionGetter level, BlockPos pos, int minY, int maxY) {
        if (pos.getY() <= minY || pos.getY() + 2 > maxY) return null;
        var floor = level.getBlockState(pos.below());
        if (unsafe(floor)) return null;
        Vec3 result = DismountHelper.findSafeDismountLocation(EntityType.PLAYER, level, pos, true);
        return result != null && safeVolume(level, EntityType.PLAYER.getDimensions().makeBoundingBox(result), minY, maxY)
            ? result : null;
    }

    private static boolean safeVolume(CollisionGetter level, AABB bounds, int minY, int maxY) {
        if (bounds.minY < minY || bounds.maxY > maxY || !level.getWorldBorder().isWithinBounds(bounds)) return false;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            var state = level.getBlockState(pos);
            if (unsafe(state)) return false;
        }
        return true;
    }

    private static boolean unsafe(BlockState state) {
        return !state.getFluidState().isEmpty() || EntityType.PLAYER.isBlockDangerous(state)
            || state.getBlock() instanceof BaseFireBlock || state.is(Blocks.POWDER_SNOW)
            || state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.END_PORTAL);
    }
}
