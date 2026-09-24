package com.epiac9.cobblemonnomanslands.expedition.selection;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Server-thread request budgets; positions cannot be varied to bypass an owner's limit. */
public final class ExplorationBoardRequestPolicy {
    public enum Action {
        STATUS(10), SELECT(5), CANCEL(5);

        private final long cooldownTicks;

        Action(long cooldownTicks) {
            this.cooldownTicks = cooldownTicks;
        }
    }

    private final Map<UUID, EnumMap<Action, Long>> lastAccepted = new HashMap<>();

    public boolean tryAcquire(UUID ownerId, Action action, long gameTime) {
        if (ownerId == null || action == null || gameTime < 0) {
            return false;
        }
        EnumMap<Action, Long> times = lastAccepted.computeIfAbsent(ownerId, id -> new EnumMap<>(Action.class));
        Long previous = times.get(action);
        if (previous != null && gameTime >= previous && gameTime - previous < action.cooldownTicks) {
            return false;
        }
        times.put(action, gameTime);
        return true;
    }

    public void forget(UUID ownerId) {
        lastAccepted.remove(ownerId);
    }

    public static boolean canAccess(double distanceSquared, boolean registered,
                                    BooleanSupplier chunkLoaded, BooleanSupplier boardPresent) {
        return distanceSquared >= 0.0D && distanceSquared <= 64.0D && registered
            && chunkLoaded.getAsBoolean() && boardPresent.getAsBoolean();
    }
}
