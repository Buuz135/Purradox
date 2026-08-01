package com.purradox.world.level.block;

import com.mojang.serialization.MapCodec;
import com.purradox.world.level.block.entity.CatSeatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CatSeatBlock extends BaseEntityBlock {
    public static final MapCodec<CatSeatBlock> CODEC = simpleCodec(CatSeatBlock::new);
    private static final VoxelShape SHAPE = box(1.0, 0.0, 1.0, 15.0, 5.0, 15.0);

    public CatSeatBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CatSeatBlockEntity(pos, state);
    }
}
