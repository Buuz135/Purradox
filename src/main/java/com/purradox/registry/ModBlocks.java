package com.purradox.registry;

import com.purradox.Purradox;
import com.purradox.world.level.block.CatSeatBlock;
import com.purradox.world.level.block.WormholeTestBlock;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Purradox.MOD_ID);

    public static final DeferredBlock<WormholeTestBlock> WORMHOLE_TEST_BLOCK = BLOCKS.registerBlock(
            "wormhole_test_block",
            WormholeTestBlock::new,
            properties -> properties
                    .strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 11)
    );

    public static final DeferredBlock<CatSeatBlock> CAT_SEAT = BLOCKS.registerBlock(
            "cat_seat",
            CatSeatBlock::new,
            properties -> properties
                    .strength(0.8F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
    );

    private ModBlocks() {
    }
}
