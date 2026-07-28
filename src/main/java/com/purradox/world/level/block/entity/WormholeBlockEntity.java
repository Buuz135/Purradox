package com.purradox.world.level.block.entity;

import com.purradox.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WormholeBlockEntity extends BlockEntity {
    public WormholeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WORMHOLE.get(), pos, state);
    }
}
