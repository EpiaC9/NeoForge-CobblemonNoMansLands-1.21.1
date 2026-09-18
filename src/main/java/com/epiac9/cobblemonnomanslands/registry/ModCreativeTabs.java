package com.epiac9.cobblemonnomanslands.registry;

import com.epiac9.cobblemonnomanslands.CobblemonNoMansLands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CobblemonNoMansLands.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CNML_TAB =
            CREATIVE_TABS.register("cnml_tab",
                    () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.cobblemonnomanslands.cnml_tab"))
                            .icon(() -> new ItemStack(ModItems.DUNGEON_PORTAL_ITEM.get()))
                            .displayItems((parameters, output)
                                    -> {
                                output.accept(ModItems.DUNGEON_PORTAL_ITEM.get());
                                output.accept(ModItems.DUNGEON_PORTAL_MARKER_ITEM.get());
                                output.accept(ModItems.EXPEDITION_BOARD_MARKER_ITEM.get());
                                output.accept(ModItems.STARTER_STRUCTURE_MARKER_ITEM.get());
                                    }
                            ).build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
