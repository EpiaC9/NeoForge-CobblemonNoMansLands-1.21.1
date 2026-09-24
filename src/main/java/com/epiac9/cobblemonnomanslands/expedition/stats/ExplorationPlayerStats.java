package com.epiac9.cobblemonnomanslands.expedition.stats;

public record ExplorationPlayerStats(int rank, int power, int partyCount, boolean available) {
    public ExplorationPlayerStats(int rank, int power, int partyCount) {
        this(rank, power, partyCount, true);
    }

    public static ExplorationPlayerStats unavailable() {
        return new ExplorationPlayerStats(0, 0, 0, false);
    }
}
