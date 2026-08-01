package com.purradox.world.item;

import com.purradox.cat.CatType;
import com.purradox.registry.ModEntities;
import com.purradox.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

public final class CatSpawnEggItem extends SpawnEggItem {
    private static final String CAT_TYPE_TAG = "CatType";

    public CatSpawnEggItem(Properties properties) {
        super(properties);
    }

    public static ItemStack forType(ResourceKey<CatType> key, CatType type) {
        ItemStack stack = new ItemStack(ModItems.CAT_SPAWN_EGG.get());
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString(CAT_TYPE_TAG, key.identifier().toString());
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(ModEntities.CAT.get(), entityTag));
        stack.set(
                DataComponents.ITEM_NAME,
                Component.translatable("item.purradox.cat_spawn_egg.named", Component.translatable(type.translationKey()))
        );
        return stack;
    }
}
