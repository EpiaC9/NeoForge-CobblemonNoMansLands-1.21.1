package com.epiac9.cobblemonnomanslands.expedition.selection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

/** Server-thread-owned pending entrances, independent of the board shared by players. */
public final class ExplorationReservationRegistry {
    private final Map<UUID, ExplorationSelectionState> reservations = new HashMap<>();

    public boolean reserve(ExplorationSelectionState selection) {
        Objects.requireNonNull(selection, "Selection cannot be null");
        return reservations.putIfAbsent(selection.ownerId(), selection) == null;
    }

    public ExplorationSelectionState get(UUID ownerId) {
        return reservations.get(ownerId);
    }

    public boolean release(UUID ownerId, String instanceId, Predicate<ExplorationSelectionState> cleanup) {
        ExplorationSelectionState selection = reservations.get(ownerId);
        if (selection == null || !selection.instanceId().equals(instanceId)) {
            return false;
        }
        // Keep ownership until world/instance cleanup succeeds, so failures can be retried.
        return cleanup.test(selection) && reservations.remove(ownerId, selection);
    }

    public boolean complete(UUID ownerId, String instanceId) {
        return release(ownerId, instanceId, selection -> true);
    }

    public void expire(long gameTime, Predicate<ExplorationSelectionState> cleanup) {
        for (ExplorationSelectionState selection : List.copyOf(reservations.values())) {
            if (gameTime >= selection.expiresAt()) {
                release(selection.ownerId(), selection.instanceId(), cleanup);
            }
        }
    }

    public void releaseAll(Predicate<ExplorationSelectionState> cleanup) {
        for (ExplorationSelectionState selection : List.copyOf(reservations.values())) {
            release(selection.ownerId(), selection.instanceId(), cleanup);
        }
    }
}
