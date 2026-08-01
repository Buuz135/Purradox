package com.purradox.cat;

import com.purradox.Purradox;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

public final class CatTypeRegistry {
    public static final ResourceKey<Registry<CatType>> CAT_TYPES = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath(Purradox.MOD_ID, "cat_type")
    );
    public static final ResourceKey<CatType> DEFAULT = key(
            Identifier.fromNamespaceAndPath(Purradox.MOD_ID, "tabby")
    );

    private CatTypeRegistry() {
    }

    public static void register(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(CAT_TYPES, CatType.CODEC, CatType.CODEC);
    }

    public static ResourceKey<CatType> key(Identifier id) {
        return ResourceKey.create(CAT_TYPES, id);
    }
}
