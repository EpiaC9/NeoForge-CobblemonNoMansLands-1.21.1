package com.epiac9.cobblemonnomanslands.structure;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class WorldSpawnStructureService {
    private WorldSpawnStructureService() {
    }

    public static void generateAtWorldSpawn(ServerLevel level) {
        if (level == null) {
            return;
        }
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }

        BlockPos spawn = level.getSharedSpawnPos();
        BlockPos ground = findGroundSpawn(level, spawn);
        BlockPos structurePos = ground.above(1);
        ResourceLocation structureId = ResourceLocation.fromNamespaceAndPath(
                CobblemonNoMansLands.MODID,
                "overworld/starter_structure"
        );

        StructureTemplateManager templateManager = level.getStructureManager();
        StructureTemplate template = templateManager.getOrCreate(structureId);
        if (template == null) {
            return;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        template.placeInWorld(
                level,
                structurePos,
                structurePos,
                settings,
                level.getRandom(),
                Block.UPDATE_ALL
        );
    }

    private static BlockPos findGroundSpawn(ServerLevel level, BlockPos spawn) {
        int x = spawn.getX();
        int z = spawn.getZ();

        for (int y = level.getMaxBuildHeight(); y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);

            if (state.isAir()) {
                continue;
            }
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS)
                    || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.REPLACEABLE)
                    || state.is(BlockTags.LOGS)) {
                continue;
            }
            return pos;
        }
        return new BlockPos(x, level.getSeaLevel(), z);
    }
}
