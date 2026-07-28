package com.purradox.registry;

import com.purradox.Purradox;
import com.purradox.world.level.block.entity.WormholeBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Purradox.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WormholeBlockEntity>> WORMHOLE =
        BLOCK_ENTITY_TYPES.register(
            "wormhole",
            () -> new BlockEntityType<>(WormholeBlockEntity::new, ModBlocks.WORMHOLE_TEST_BLOCK.get())
        );

    private ModBlockEntities() {
    }
}
