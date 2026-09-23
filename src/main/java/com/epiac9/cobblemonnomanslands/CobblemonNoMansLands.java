package com.epiac9.cobblemonnomanslands;

import com.epiac9.cobblemonnomanslands.datagen.ModDataGenerators;
import com.epiac9.cobblemonnomanslands.network.ModNetwork;
import com.epiac9.cobblemonnomanslands.registry.ModBlocks;
import com.epiac9.cobblemonnomanslands.registry.ModCreativeTabs;
import com.epiac9.cobblemonnomanslands.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(CobblemonNoMansLands.MODID)
public class CobblemonNoMansLands {
    public static final String MODID = "cobblemonnomanslands";

    public CobblemonNoMansLands(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModDataGenerators.register(modEventBus);
        modEventBus.addListener(ModNetwork::register);
    }
}
