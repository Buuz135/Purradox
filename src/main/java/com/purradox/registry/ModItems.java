package com.purradox.registry;

import com.purradox.Purradox;
import com.purradox.world.item.CatSpawnEggItem;
import com.purradox.world.item.CatTeaserWandItem;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Purradox.MOD_ID);

    public static final DeferredItem<BlockItem> WORMHOLE_TEST_BLOCK =
            ITEMS.registerSimpleBlockItem("wormhole_test_block", ModBlocks.WORMHOLE_TEST_BLOCK);

    public static final DeferredItem<BlockItem> CAT_SEAT =
            ITEMS.registerSimpleBlockItem("cat_seat", ModBlocks.CAT_SEAT);

    public static final DeferredItem<CatTeaserWandItem> CAT_TEASER_WAND = ITEMS.registerItem(
            "cat_teaser_wand",
            CatTeaserWandItem::new,
            properties -> properties.stacksTo(1)
    );

    public static final DeferredItem<CatSpawnEggItem> CAT_SPAWN_EGG = ITEMS.registerItem(
            "cat_spawn_egg",
            CatSpawnEggItem::new,
            properties -> properties.spawnEgg(ModEntities.CAT.get())
    );

    private ModItems() {
    }
}
