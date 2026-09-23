package com.epiac9.cobblemonnomanslands.datagen;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.portal.DungeonPortalBlock;
import com.epiac9.cobblemonnomanslands.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider{
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CobblemonNoMansLands.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile inactiveModel = models().withExistingParent("dungeon_portal_inactive", "minecraft:block/block")
                .texture("all", modLoc("block/portal/dungeon_portal_inactive"))
                .texture("particle", modLoc("block/portal/dungeon_portal_inactive"))
                .element().from(0,0,0).to(16,10,16).allFaces(
                        (direction, faceBuilder) -> faceBuilder.texture("#all"))
                .end();
        simpleBlockItem(ModBlocks.DUNGEON_PORTAL.get(), inactiveModel);

        ModelFile markerPortal = models().cubeAll("dungeon_portal_marker", modLoc("block/marker/dungeon_portal_marker"));
        simpleBlock(ModBlocks.DUNGEON_PORTAL_MARKER.get(), markerPortal);

        ModelFile markerBoard = models().cubeAll("expedition_board_marker", modLoc("block/marker/expedition_board_marker"));
        simpleBlock(ModBlocks.EXPEDITION_BOARD_MARKER.get(), markerBoard);

        ModelFile markerSpawn = models().cubeAll("starter_structure_marker", modLoc("block/marker/starter_structure_marker"));
        simpleBlock(ModBlocks.STARTER_STRUCTURE_MARKER.get(), markerSpawn);
    }
}
