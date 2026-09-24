package com.epiac9.cobblemonnomanslands.expedition.stats;

import net.minecraft.server.level.ServerPlayer;


public final class ExplorationPlayerStatsService {
    public ExplorationPlayerStats get(ServerPlayer player) {
        if (player == null) {
            return new ExplorationPlayerStats(0, 0, 0);
        }

        try {
            Class<?> cobblemonClass = Class.forName("com.cobblemon.mod.common.Cobblemon");
            Object cobblemon = cobblemonClass.getField("INSTANCE").get(null);
            Object storage = cobblemonClass.getMethod("getStorage").invoke(cobblemon);
            Object party = storage.getClass().getMethod("getParty", ServerPlayer.class).invoke(storage, player);
            int power = 0;
            int partyCount = 0;
            if (party instanceof Iterable<?> members) {
                for (Object pokemon : members) {
                    if (pokemon != null) {
                        partyCount++;
                        power += pokemonPower(pokemon);
                    }
                }
            }

            Class<?> expeditionsClass = Class.forName("com.cobblemonexpeditions.CobblemonExpeditions");
            Object expeditions = expeditionsClass.getField("INSTANCE").get(null);
            Object manager = expeditionsClass.getMethod("getManager").invoke(expeditions);
            Object expeditionData = manager.getClass().getMethod("get", java.util.UUID.class)
                    .invoke(manager, player.getUUID());
            int rank = expeditionData == null ? 0
                    : (int) expeditionData.getClass().getMethod("getExpeditionRank").invoke(expeditionData);
            return new ExplorationPlayerStats(rank, power, partyCount);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return new ExplorationPlayerStats(0, 0, 0);
        }
    }

    private int pokemonPower(Object pokemon) throws ReflectiveOperationException {
        int level = (int) pokemon.getClass().getMethod("getLevel").invoke(pokemon);
        Object ivs = pokemon.getClass().getMethod("getIvs").invoke(pokemon);
        Object evs = pokemon.getClass().getMethod("getEvs").invoke(pokemon);
        int ivTotal = ivs == null ? 0 : (int) ivs.getClass().getMethod("getEffectiveBattleTotal").invoke(ivs);
        int evTotal = evs == null ? 0 : (int) evs.getClass().getMethod("total").invoke(evs);
        double ivAverage = ivTotal / 6.0D;
        int ivContribution = (int) Math.round(level * ivAverage / 300.0D);
        int evContribution = (int) Math.round(evTotal * 10.0D / 510.0D);
        return level + ivContribution + evContribution;
    }
}