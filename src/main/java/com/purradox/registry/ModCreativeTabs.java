package com.purradox.registry;

import com.purradox.Purradox;
import com.purradox.cat.CatTypeRegistry;
import com.purradox.world.item.CatSpawnEggItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Comparator;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Purradox.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PURRADOX = CREATIVE_MODE_TABS.register(
            "purradox",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.purradox"))
                    .icon(() -> new ItemStack(ModItems.CAT_TEASER_WAND.get()))
                    .displayItems(ModCreativeTabs::addItems)
                    .build()
    );

    private static void addItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        ModItems.ITEMS.getEntries().stream()
                .filter(holder -> holder.value() != ModItems.CAT_SPAWN_EGG.get())
                .sorted(Comparator.comparing(holder -> holder.getId().toString()))
                .forEach(holder -> output.accept(holder.value()));

        parameters.holders().lookup(CatTypeRegistry.CAT_TYPES).ifPresentOrElse(
                catTypes -> catTypes.listElements()
                        .sorted(Comparator.comparing(holder -> holder.key().identifier()))
                        .forEach(holder -> output.accept(CatSpawnEggItem.forType(holder.key(), holder.value()))),
                () -> output.accept(ModItems.CAT_SPAWN_EGG)
        );
    }

    private ModCreativeTabs() {
    }
}
