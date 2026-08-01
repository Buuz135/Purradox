package com.purradox.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.animal.feline.BabyCatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

final class ResourcefulCatCollarLayer extends RenderLayer<
        ResourcefulCatRenderState,
        AbstractFelineModel<CatRenderState>
        > {
    private static final Identifier ADULT_TEXTURE = Identifier.withDefaultNamespace("textures/entity/cat/cat_collar.png");
    private static final Identifier BABY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/cat/cat_collar_baby.png");
    private final AdultCatModel adultModel;
    private final BabyCatModel babyModel;

    ResourcefulCatCollarLayer(ResourcefulCatRenderer renderer, EntityRendererProvider.Context context) {
        super(renderer);
        this.adultModel = new AdultCatModel(context.bakeLayer(ModelLayers.CAT_COLLAR));
        this.babyModel = new BabyCatModel(context.bakeLayer(ModelLayers.CAT_BABY_COLLAR));
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            ResourcefulCatRenderState state,
            float yRot,
            float xRot
    ) {
        DyeColor color = state.collarColor;
        if (color == null) {
            return;
        }
        AbstractFelineModel<CatRenderState> model = state.isBaby ? this.babyModel : this.adultModel;
        Identifier texture = state.isBaby ? BABY_TEXTURE : ADULT_TEXTURE;
        coloredCutoutModelCopyLayerRender(
                model,
                texture,
                poseStack,
                submitNodeCollector,
                lightCoords,
                state,
                color.getTextureDiffuseColor(),
                2
        );
    }
}
