package com.epiac9.cobblemonnomanslands.expedition.manager.route;

import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionMapping;
import com.epiac9.cobblemonnomanslands.expedition.dimension.ExpeditionDimensionProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

public class InitialExpeditionMappings {
    private InitialExpeditionMappings() {
    }

    public static ExpeditionDimensionMapping create() {
        return new ExpeditionDimensionMapping(Map.ofEntries(
            profile("berry_grove_harvest", "berry_grove", 90, "berries", "berry_bushes"),
            profile("forest_forage", "forest", 45, "forest_plants", "saplings"),
            profile("shoreline_survey", "shoreline", 60, "shoreline_plants", "kelp"),
            profile("cave_delve", "cavern", 120, "cave_resources", "ores"),
            profile("volcanic_survey", "volcanic", 225, "volcanic_resources"),
            profile("deep_sea_dive", "deep_sea", 180, "kelp", "water_vegetation"),
            profile("frozen_ruins", "frozen_ruins", 338, "ice_resources", "frozen_plants"),
            profile("distortion_rift", "distortion_rift", 450, "rift_resources")
        )); //map expedition id to profiles
    }

        private static Map.Entry<ResourceLocation, ExpeditionDimensionProfile> profile(String expeditionId,
                                                String dimensionId, int rankZeroPower,
                                                String... resourceCategories) {
        return Map.entry(
            ResourceLocation.parse("cobblemon_expeditions:" + expeditionId),
            new ExpeditionDimensionProfile(
                ResourceLocation.parse("cobblemonnomanslands:" + dimensionId),
                20L * 60L * 5L,
                Set.of(resourceCategories),
                rankRequirements(rankZeroPower),
                3));
        }

        private static Map<Integer, Integer> rankRequirements(int rankZeroPower) {
            Map<Integer, Integer> requirements = new LinkedHashMap<>();
            for (int rank = 0; rank <= 10; rank++) {
                requirements.put(rank, (int) Math.round(rankZeroPower * rankMultiplier(rank)));
            }
            return requirements;
        }

        private static double rankMultiplier(int rank) {
            return switch (rank) {
                case 0 -> 1.0D;
                case 1 -> 1.2D;
                case 2 -> 1.44D;
                case 3 -> 1.72D;
                case 4 -> 2.04D;
                case 5 -> 2.40D;
                case 6 -> 2.76D;
                case 7 -> 3.12D;
                case 8 -> 3.44D;
                case 9 -> 3.72D;
                default -> 4.0D;
            };
        }
}
