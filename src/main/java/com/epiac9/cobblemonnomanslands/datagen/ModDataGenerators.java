package com.epiac9.cobblemonnomanslands.datagen;

import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class ModDataGenerators {
    private ModDataGenerators() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModDataGenerators::gatherData);
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeClient(), new ModLanguageProvider(event.getGenerator().getPackOutput()));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(event.getGenerator().getPackOutput(),
                event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(event.getGenerator().getPackOutput(),
                event.getExistingFileHelper()));
    }
}
