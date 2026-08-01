package com.purradox.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.purradox.Purradox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class WormholeRenderTypes {
    private static final Identifier SHADER = id("core/wormhole");

    public static final RenderPipeline WORMHOLE_PIPELINE = RenderPipeline.builder(
                    RenderPipelines.MATRICES_FOG_SNIPPET,
                    RenderPipelines.GLOBALS_SNIPPET
            )
            .withLocation(id("pipeline/wormhole"))
            .withVertexShader(SHADER)
            .withFragmentShader(SHADER)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withCull(false)
            .build();

    public static final RenderType WORMHOLE = RenderType.create(
            "purradox_wormhole",
            RenderSetup.builder(WORMHOLE_PIPELINE)
                    .sortOnUpload()
                    .createRenderSetup()
    );

    private WormholeRenderTypes() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Purradox.MOD_ID, path);
    }
}
