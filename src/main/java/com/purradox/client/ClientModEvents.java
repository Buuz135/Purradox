package com.purradox.client;

import com.purradox.Purradox;
import com.purradox.client.renderer.ResourcefulCatRenderer;
import com.purradox.client.renderer.WormholeBlockEntityRenderer;
import com.purradox.client.renderer.WormholeRenderTypes;
import com.purradox.registry.ModBlockEntities;
import com.purradox.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = Purradox.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(WormholeRenderTypes.WORMHOLE_PIPELINE);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.WORMHOLE.get(), WormholeBlockEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.CAT.get(), ResourcefulCatRenderer::new);
    }
}
