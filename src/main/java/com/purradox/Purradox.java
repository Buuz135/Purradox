package com.purradox;

import com.purradox.registry.ModBlockEntities;
import com.purradox.registry.ModBlocks;
import com.purradox.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Purradox.MOD_ID)
public final class Purradox {
    public static final String MOD_ID = "purradox";

    public Purradox(IEventBus modBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        modBus.addListener(Purradox::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(ModItems.WORMHOLE_TEST_BLOCK);
        }
    }
}
