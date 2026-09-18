package com.epiac9.cobblemonnomanslands.registry;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import com.epiac9.cobblemonnomanslands.marker.BoardMarkerBlock;
import com.epiac9.cobblemonnomanslands.marker.StarterStructureMarker;
import com.epiac9.cobblemonnomanslands.portal.DungeonPortalBlock;
import com.epiac9.cobblemonnomanslands.marker.PortalMarkerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CobblemonNoMansLands.MODID);

    public static final DeferredBlock<Block> DUNGEON_PORTAL = BLOCKS.register("dungeon_portal",
            () -> new DungeonPortalBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f).noOcclusion().noCollission()));
    public static final DeferredBlock<Block> DUNGEON_PORTAL_MARKER = BLOCKS.register("dungeon_portal_marker",
            () -> new PortalMarkerBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f).noOcclusion()));
    public static final DeferredBlock<Block> EXPEDITION_BOARD_MARKER = BLOCKS.register("expedition_board_marker",
            () -> new BoardMarkerBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f).noOcclusion()));
    public static final DeferredBlock<Block> STARTER_STRUCTURE_MARKER = BLOCKS.register("starter_structure_marker",
            () -> new StarterStructureMarker(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f).noOcclusion()));

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
