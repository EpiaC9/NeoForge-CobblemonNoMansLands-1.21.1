package com.epiac9.cobblemonnomanslands.expedition.stats;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemonexpeditions.CobblemonExpeditions;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class ExplorationPlayerStatsService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long LOG_INTERVAL_NANOS = 60_000_000_000L;
    private final LongSupplier clock;
    private final Consumer<Throwable> reportFailure;
    private boolean hasLoggedFailure;
    private long lastFailureLog;

    public ExplorationPlayerStatsService() {
        this(System::nanoTime, error -> LOGGER.warn("Unable to read Exploration party/rank stats; Confirm is disabled until lookup recovers", error));
    }

    ExplorationPlayerStatsService(LongSupplier clock, Consumer<Throwable> reportFailure) {
        this.clock = clock;
        this.reportFailure = reportFailure;
    }

    public ExplorationPlayerStats get(ServerPlayer player) {
        return player == null ? ExplorationPlayerStats.unavailable() : read(() -> {
            var party = Objects.requireNonNull(Cobblemon.INSTANCE.getStorage().getParty(player), "Party storage unavailable");
            int power = 0;
            int partyCount = 0;
            for (var pokemon : party) {
                if (pokemon != null) {
                    partyCount++;
                    power += pokemonPower(pokemon.getLevel(), pokemon.getIvs().getEffectiveBattleTotal(),
                        pokemon.getEvs().total());
                }
            }
            var data = Objects.requireNonNull(CobblemonExpeditions.INSTANCE.getManager().get(player.getUUID()),
                "Expedition rank data unavailable");
            return new ExplorationPlayerStats(data.getExpeditionRank(), power, partyCount);
        });
    }

    ExplorationPlayerStats read(Supplier<ExplorationPlayerStats> lookup) {
        try {
            return Objects.requireNonNull(lookup.get(), "Stats lookup returned null");
        } catch (RuntimeException | LinkageError error) {
            long now = clock.getAsLong();
            if (!hasLoggedFailure || now - lastFailureLog >= LOG_INTERVAL_NANOS) {
                hasLoggedFailure = true;
                lastFailureLog = now;
                reportFailure.accept(error);
            }
            return ExplorationPlayerStats.unavailable();
        }
    }

    static int pokemonPower(int level, int ivTotal, int evTotal) {
        double ivAverage = ivTotal / 6.0D;
        return level + (int) Math.round(level * ivAverage / 300.0D)
            + (int) Math.round(evTotal * 10.0D / 510.0D);
    }
}
