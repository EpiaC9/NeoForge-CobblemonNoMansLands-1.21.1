package com.epiac9.cobblemonnomanslands.expedition;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ExpeditionDungeonMapping {
    private final Map<ResourceLocation, String> expeditionToDungeon;

    public ExpeditionDungeonMapping(Map<ResourceLocation, String> expeditionToDungeon) {
        this.expeditionToDungeon = new HashMap<>(expeditionToDungeon);
    }

    public String getDungeonKey(ResourceLocation expeditionId) {
        return expeditionToDungeon.get(expeditionId);
    }

    public boolean contains(ResourceLocation expeditionId) {
        return expeditionToDungeon.containsKey(expeditionId);
    }
}
