package com.epiac9.cobblemonnomanslands.datagen;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CobblemonNoMansLands.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("dungeon_portal_item", modLoc("block/dungeon_portal_inactive"));
        withExistingParent("dungeon_portal_marker_item", modLoc("block/dungeon_portal_marker"));
        withExistingParent("expedition_board_marker_item", modLoc("block/expedition_board_marker"));
        withExistingParent("starter_structure_marker_item", modLoc("block/starter_structure_marker"));
        withExistingParent("dungeon_boundary_item", modLoc("block/dungeon_boundary"));
    }
}
