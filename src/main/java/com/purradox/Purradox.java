package com.purradox;

import com.purradox.cat.CatTypeRegistry;
import com.purradox.registry.ModBlockEntities;
import com.purradox.registry.ModBlocks;
import com.purradox.registry.ModCreativeTabs;
import com.purradox.registry.ModEntities;
import com.purradox.registry.ModItems;
import com.purradox.world.item.CatSpawnEggItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import java.util.Comparator;

@Mod(Purradox.MOD_ID)
public final class Purradox {
    public static final String MOD_ID = "purradox";

    public Purradox(IEventBus modBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        modBus.addListener(CatTypeRegistry::register);
        modBus.addListener(Purradox::registerEntityAttributes);
        modBus.addListener(Purradox::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(ModItems.WORMHOLE_TEST_BLOCK);
            event.accept(ModItems.CAT_SEAT);
            event.accept(ModItems.CAT_TEASER_WAND);
        }
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) {
            event.getParameters().holders().lookup(CatTypeRegistry.CAT_TYPES).ifPresentOrElse(
                    catTypes -> catTypes.listElements()
                            .sorted(Comparator.comparing(holder -> holder.key().identifier()))
                            .forEach(holder -> event.accept(CatSpawnEggItem.forType(holder.key(), holder.value()))),
                    () -> event.accept(ModItems.CAT_SPAWN_EGG)
            );
        }
    }

    private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CAT.get(), net.minecraft.world.entity.animal.feline.Cat.createAttributes().build());
    }
}
