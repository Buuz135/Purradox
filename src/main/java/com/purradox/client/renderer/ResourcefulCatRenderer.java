package com.purradox.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.purradox.cat.CatType;
import com.purradox.world.entity.ResourcefulCat;
import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.animal.feline.BabyCatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ResourcefulCatRenderer extends AgeableMobRenderer<
        ResourcefulCat,
        ResourcefulCatRenderState,
        AbstractFelineModel<CatRenderState>
        > {
    private final ItemModelResolver itemModelResolver;

    public ResourcefulCatRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new AdultCatModel(context.bakeLayer(ModelLayers.CAT)),
                new BabyCatModel(context.bakeLayer(ModelLayers.CAT_BABY)),
                0.4F
        );
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new OverlayLayer(this));
        this.addLayer(new ResourcefulCatCollarLayer(this, context));
        this.addLayer(new AttachmentLayer(this));
    }

    @Override
    public Identifier getTextureLocation(ResourcefulCatRenderState state) {
        return state.texture;
    }

    @Override
    public ResourcefulCatRenderState createRenderState() {
        return new ResourcefulCatRenderState();
    }

    @Override
    public void extractRenderState(ResourcefulCat entity, ResourcefulCatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Identifier defaultTexture = entity.getVariant().value().assetInfo(state.isBaby).texturePath();
        state.texture = entity.getCatType()
                .flatMap(holder -> holder.value().texture())
                .orElse(defaultTexture);
        state.overlay = entity.getCatType()
                .flatMap(holder -> holder.value().overlay())
                .orElse(null);
        state.isCrouching = entity.isCrouching();
        state.isSprinting = entity.isSprinting();
        state.isSitting = entity.isInSittingPose();
        state.lieDownAmount = entity.getLieDownAmount(partialTicks);
        state.lieDownAmountTail = entity.getLieDownAmountTail(partialTicks);
        state.relaxStateOneAmount = entity.getRelaxStateOneAmount(partialTicks);
        state.isLyingOnTopOfSleepingPlayer = entity.isLyingOnTopOfSleepingPlayer();
        state.collarColor = entity.isTame() ? entity.getCollarColor() : null;
        state.isTamed = entity.isTame();

        state.attachments.clear();
        entity.getCatType().ifPresent(holder -> {
            for (CatType.Attachment attachment : holder.value().attachments()) {
                if (!attachment.visibility().isVisible(entity.isTame(), entity.isBaby(), entity.isInSittingPose())) {
                    continue;
                }
                ItemStack carrier = new ItemStack(Items.PAPER);
                carrier.set(DataComponents.ITEM_MODEL, attachment.model());
                ItemStackRenderState itemState = new ItemStackRenderState();
                this.itemModelResolver.updateForLiving(itemState, carrier, ItemDisplayContext.FIXED, entity);
                state.attachments.add(new ResourcefulCatRenderState.AttachmentState(attachment, itemState));
            }
        });
    }

    @Override
    protected void setupRotations(ResourcefulCatRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        float amount = state.lieDownAmount;
        if (amount > 0.0F) {
            poseStack.translate(0.4F * amount, 0.15F * amount, 0.1F * amount);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(amount, 0.0F, 90.0F)));
            if (state.isLyingOnTopOfSleepingPlayer) {
                poseStack.translate(0.15F * amount, 0.0F, 0.0F);
            }
        }
    }

    private static final class OverlayLayer extends RenderLayer<
            ResourcefulCatRenderState,
            AbstractFelineModel<CatRenderState>
            > {
        private OverlayLayer(ResourcefulCatRenderer renderer) {
            super(renderer);
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
            if (state.overlay != null) {
                coloredCutoutModelCopyLayerRender(
                        this.getParentModel(),
                        state.overlay,
                        poseStack,
                        submitNodeCollector,
                        lightCoords,
                        state,
                        -1,
                        1
                );
            }
        }
    }

    private static final class AttachmentLayer extends RenderLayer<
            ResourcefulCatRenderState,
            AbstractFelineModel<CatRenderState>
            > {
        private AttachmentLayer(ResourcefulCatRenderer renderer) {
            super(renderer);
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
            for (ResourcefulCatRenderState.AttachmentState attachmentState : state.attachments) {
                ItemStackRenderState item = attachmentState.item();
                if (item.isEmpty()) {
                    continue;
                }

                CatType.Attachment attachment = attachmentState.definition();
                CatType.Transform transform = attachment.transform();
                poseStack.pushPose();

                ModelPart root = this.getParentModel().root();
                root.translateAndRotate(poseStack);
                if (attachment.anchor() != CatType.Anchor.ROOT) {
                    root.getChild(attachment.anchor().partName()).translateAndRotate(poseStack);
                }

                poseStack.translate(
                        transform.translation().x(),
                        transform.translation().y(),
                        transform.translation().z()
                );
                poseStack.mulPose(Axis.XP.rotationDegrees(transform.rotation().x()));
                poseStack.mulPose(Axis.YP.rotationDegrees(transform.rotation().y()));
                poseStack.mulPose(Axis.ZP.rotationDegrees(transform.rotation().z()));
                poseStack.scale(transform.scale().x(), transform.scale().y(), transform.scale().z());
                item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }
    }
}
