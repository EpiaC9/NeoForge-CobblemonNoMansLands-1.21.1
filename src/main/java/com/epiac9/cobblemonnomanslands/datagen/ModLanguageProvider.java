package com.epiac9.cobblemonnomanslands.datagen;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, CobblemonNoMansLands.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.cobblemonnomanslands.cnml_tab", "Cobblemon No Man's Land");

        add("block.cobblemonnomanslands.dungeon_portal", "Dungeon Portal");
        add("block.cobblemonnomanslands.dungeon_portal_marker", "Dungeon Portal Marker");
        add("block.cobblemonnomanslands.expedition_board_marker", "Expedition Board Marker");
        add("block.cobblemonnomanslands.starter_structure_marker", "Starter Structure Marker");
        add("block.cobblemonnomanslands.dungeon_boundary", "Dungeon Boundary");

        add("item.cobblemonnomanslands.dungeon_portal_item", "Dungeon Portal");
        add("item.cobblemonnomanslands.dungeon_portal_marker_item", "Dungeon Portal Marker");
        add("item.cobblemonnomanslands.expedition_board_marker", "Expedition Board Marker");
        add("item.cobblemonnomanslands.starter_structure_marker", "Starter Structure Marker");
        add("item.cobblemonnomanslands.dungeon_boundary", "Dungeon Boundary");
    }
}
