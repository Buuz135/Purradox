package com.purradox.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public final class WormholeRenderState extends BlockEntityRenderState {
    public final StrugglingCatRenderState cat = new StrugglingCatRenderState();
    public float emergence;
    public float struggle;
}
