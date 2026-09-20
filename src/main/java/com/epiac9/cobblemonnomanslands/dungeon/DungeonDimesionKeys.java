package com.epiac9.cobblemonnomanslands.dungeon;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class DungeonDimesionKeys {
    public static final ResourceKey<Level> DUNGEON = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(CobblemonNoMansLands.MODID, "dungeon"));

    private DungeonDimesionKeys() {
    }
}
