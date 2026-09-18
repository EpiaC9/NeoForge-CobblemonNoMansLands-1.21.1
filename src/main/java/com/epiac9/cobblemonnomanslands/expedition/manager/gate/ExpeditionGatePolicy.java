package com.epiac9.cobblemonnomanslands.expedition.manager.gate;

import com.cobblemonexpeditions.data.ExpeditionDefinition;
import com.epiac9.cobblemonnomanslands.expedition.ExpeditionDungeonMapping;

public class ExpeditionGatePolicy {
    private static final int PARTY_CAP = 6;
    private final ExpeditionDungeonMapping mapping;

    public ExpeditionGatePolicy(ExpeditionDungeonMapping mapping) {
        if (mapping == null) {
            throw new NullPointerException("Mapping cannot be null");
        }
        this.mapping = mapping;
    }

    public boolean canRoute(ExpeditionDefinition expedition) {
        return expedition != null && mapping.contains(expedition.getId());
    }

    public int getPartyLimit(ExpeditionDefinition expedition) {
        if (expedition == null) {
            throw new NullPointerException("Expedition cannot be null");
        }
        return Math.min(PARTY_CAP, expedition.getMaxPartySize());
    }

    public boolean isPartySizeAllowed(ExpeditionDefinition expedition, int selectedPokemonCount) {
        if(expedition == null) {
            return false;
        }
        int partyLimit = getPartyLimit(expedition);
        return selectedPokemonCount >= expedition.getMinPartySize() && selectedPokemonCount <= partyLimit;
    }
}
