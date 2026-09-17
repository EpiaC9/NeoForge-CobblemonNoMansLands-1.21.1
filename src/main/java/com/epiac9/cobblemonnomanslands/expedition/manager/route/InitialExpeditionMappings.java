package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.epiac9.cobblemonnomanslands.expedition.ExpeditionDungeonMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class InitialExpeditionMappings {
    private InitialExpeditionMappings() {
    }

    public static ExpeditionDungeonMapping create() {
        return new ExpeditionDungeonMapping(Map.ofEntries(
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:berry_grove_harvest"), "nml_berry_grove"),
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:forest_forage"), "nml_forest"),
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:shoreline_survey"), "nml_shoreline"),
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:cave_delve"), "nml_cavern"),
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:volcanic_survey"), "nml_volcanic"),
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:deep_sea_dive"), "nml_deep_sea"),
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:frozen_ruins"), "nml_frozen_ruins"),
                Map.entry(ResourceLocation.parse("cobblemon_expeditions:distortion_rift"), "nml_distortion_rift")
        )); //map expedition id to profiles
    }
}
