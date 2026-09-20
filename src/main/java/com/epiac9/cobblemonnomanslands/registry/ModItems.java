package com.epiac9.cobblemonnomanslands.registry;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonNoMansLands.MODID);

    public static final DeferredItem<BlockItem> DUNGEON_PORTAL_ITEM = ITEMS.register("dungeon_portal_item",
            () -> new BlockItem(ModBlocks.DUNGEON_PORTAL.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> DUNGEON_PORTAL_MARKER_ITEM = ITEMS.register("dungeon_portal_marker_item",
            () -> new BlockItem(ModBlocks.DUNGEON_PORTAL_MARKER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> EXPEDITION_BOARD_MARKER_ITEM = ITEMS.register("expedition_board_marker_item",
            () -> new BlockItem(ModBlocks.EXPEDITION_BOARD_MARKER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> STARTER_STRUCTURE_MARKER_ITEM = ITEMS.register("starter_structure_marker_item",
            () -> new BlockItem(ModBlocks.STARTER_STRUCTURE_MARKER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> DUNGEON_BOUNDARY_ITEM = ITEMS.register("dungeon_boundary_item",
            () -> new BlockItem(ModBlocks.DUNGEON_BOUNDARY.get(), new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
