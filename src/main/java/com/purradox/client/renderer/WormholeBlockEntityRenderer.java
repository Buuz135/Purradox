package com.purradox.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.purradox.world.level.block.entity.WormholeBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class WormholeBlockEntityRenderer
        implements BlockEntityRenderer<WormholeBlockEntity, WormholeRenderState> {

    private static final float CYCLE_TICKS = 220.0F;
    private static final Identifier CAT_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/cat/cat_red.png");

    private final StrugglingCatModel catModel;

    public WormholeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.catModel = new StrugglingCatModel(context.bakeLayer(ModelLayers.CAT));
    }

    @Override
    public WormholeRenderState createRenderState() {
        return new WormholeRenderState();
    }

    @Override
    public void extractRenderState(
            WormholeBlockEntity blockEntity,
            WormholeRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        float gameTime = blockEntity.getLevel() == null
                ? partialTicks
                : blockEntity.getLevel().getGameTime() + partialTicks;
        float blockOffset = Math.floorMod(blockEntity.getBlockPos().hashCode(), (int) CYCLE_TICKS);
        float phase = Mth.frac((gameTime + blockOffset) / CYCLE_TICKS);

        state.emergence = emergenceAt(phase);
        state.struggle = struggleAt(phase);
        state.cat.emergence = state.emergence;
        state.cat.grip = gripAt(phase);
        state.cat.pullProgress = smoothStep(0.28F, 0.68F, phase);
        state.cat.push = pushAt(phase) * state.cat.grip;
        state.cat.ageInTicks = gameTime;
        state.cat.walkAnimationPos = gameTime * 1.7F;
        state.cat.walkAnimationSpeed = state.struggle;
        state.cat.yRot = Mth.sin(gameTime * 0.22F) * 12.0F * state.struggle;
        state.cat.xRot = -8.0F - Mth.abs(Mth.sin(gameTime * 0.31F)) * 10.0F * state.struggle;
        state.cat.lightCoords = state.lightCoords;
    }

    @Override
    public void submit(
            WormholeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.003F, 0.0F);
        submitNodeCollector.submitCustomGeometry(poseStack, WormholeRenderTypes.WORMHOLE, (pose, buffer) -> {
            buffer.addVertex(pose, 0.03F, 0.0F, 0.03F).setUv(0.0F, 0.0F);
            buffer.addVertex(pose, 0.03F, 0.0F, 0.97F).setUv(0.0F, 1.0F);
            buffer.addVertex(pose, 0.97F, 0.0F, 0.97F).setUv(1.0F, 1.0F);
            buffer.addVertex(pose, 0.97F, 0.0F, 0.03F).setUv(1.0F, 0.0F);
        });
        poseStack.popPose();

        if (state.emergence > 0.005F) {
            submitCat(state, poseStack, submitNodeCollector);
        }
    }

    private void submitCat(WormholeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        float heave = Mth.sin(state.cat.ageInTicks * 0.72F)
                * 0.035F
                * state.struggle
                * (1.0F - state.cat.grip);
        float victoryBounce = state.emergence > 0.96F
                ? Mth.abs(Mth.sin(state.cat.ageInTicks * 0.45F)) * 0.035F
                : 0.0F;

        poseStack.pushPose();
        poseStack.translate(
                0.5F,
                verticalRootY(state.emergence) + heave + victoryBounce + 0.15f,
                0.85F
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(-78.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.sin(state.cat.ageInTicks * 0.67F) * (3.0F + state.cat.push * 4.0F) * state.struggle
        ));
        poseStack.scale(-0.9F, -0.9F, 0.9F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        submitNodeCollector.submitModel(
                this.catModel,
                state.cat,
                poseStack,
                CAT_TEXTURE,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0,
                state.breakProgress
        );
        poseStack.popPose();
    }

    private static float verticalRootY(float emergence) {
        if (emergence <= 0.6F) {
            return 0.05F + emergence * 1.2F;
        }
        return 0.77F + (emergence - 0.6F) * 1.75F;
    }

    private static float emergenceAt(float phase) {
        if (phase < 0.12F) {
            return 0.0F;
        }
        if (phase < 0.28F) {
            return 0.3F * smoothStep(0.12F, 0.28F, phase);
        }
        if (phase < 0.68F) {
            float progress = smoothStep(0.28F, 0.68F, phase);
            float push = Mth.abs(Mth.sin(progress * 3.0F * Mth.PI));
            return 0.3F + progress * 0.3F + push * 0.055F;
        }
        if (phase < 0.77F) {
            float pop = smoothStep(0.68F, 0.77F, phase);
            float overshoot = Mth.sin(pop * Mth.PI) * 0.1F;
            return 0.6F + pop * 0.4F + overshoot;
        }
        if (phase < 0.92F) {
            return 1.0F;
        }
        return 1.0F - smoothStep(0.92F, 1.0F, phase);
    }

    private static float struggleAt(float phase) {
        float gettingStuck = smoothStep(0.2F, 0.3F, phase) * (1.0F - smoothStep(0.68F, 0.79F, phase));
        float gettingPulledBack = smoothStep(0.92F, 0.95F, phase);
        return Mth.clamp(gettingStuck + gettingPulledBack, 0.0F, 1.0F);
    }

    private static float gripAt(float phase) {
        return smoothStep(0.22F, 0.3F, phase) * (1.0F - smoothStep(0.68F, 0.76F, phase));
    }

    private static float pushAt(float phase) {
        float progress = Mth.clamp((phase - 0.28F) / 0.4F, 0.0F, 1.0F);
        return Mth.abs(Mth.sin(progress * 3.0F * Mth.PI));
    }

    private static float smoothStep(float start, float end, float value) {
        float x = Mth.clamp((value - start) / (end - start), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    @Override
    public AABB getRenderBoundingBox(WormholeBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).expandTowards(0.0, 1.5, 0.0);
    }
}
