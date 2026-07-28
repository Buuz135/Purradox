package com.purradox.registry;

import com.purradox.Purradox;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Purradox.MOD_ID);

    public static final DeferredItem<BlockItem> WORMHOLE_TEST_BLOCK =
        ITEMS.registerSimpleBlockItem("wormhole_test_block", ModBlocks.WORMHOLE_TEST_BLOCK);

    private ModItems() {
    }
}
