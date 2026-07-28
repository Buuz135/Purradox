package com.purradox.client.renderer;

import net.minecraft.client.model.animal.feline.AdultCatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.util.Mth;

public final class StrugglingCatModel extends AdultCatModel {
    public StrugglingCatModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(CatRenderState state) {
        super.setupAnim(state);

        StrugglingCatRenderState struggleState = (StrugglingCatRenderState) state;
        float effort = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
        float grip = struggleState.grip;
        float push = struggleState.push;
        float time = state.ageInTicks;
        float heave = Mth.sin(time * 0.72F);
        float freeStruggle = effort * (1.0F - grip);

        this.body.xRot += (0.11F - push * 0.2F) * grip;
        this.body.zRot += heave * 0.08F * effort;
        this.body.y -= push * 0.8F * grip;
        this.head.y -= push * 0.65F * grip;
        this.head.zRot += Mth.sin(time * 1.1F + 0.4F) * 0.13F * effort;
        this.head.yRot += heave * 0.2F * effort;
        this.head.xRot -= (0.1F + push * 0.16F) * effort - 1f;

        float plantedLegAngle = Mth.lerp(struggleState.pullProgress, -0.58F, 0.08F);
        float reachingLegAngle = -0.95F + Mth.sin(time * 0.8F) * 0.1F;
        float pawTension = Mth.sin(time * 1.35F) * 0.008F * grip;
        this.leftFrontLeg.xRot = Mth.lerp(
            grip,
            Mth.lerp(freeStruggle, this.leftFrontLeg.xRot, reachingLegAngle),
            plantedLegAngle + pawTension
        );
        this.rightFrontLeg.xRot = Mth.lerp(
            grip,
            Mth.lerp(freeStruggle, this.rightFrontLeg.xRot, reachingLegAngle),
            plantedLegAngle + pawTension
        );
        this.leftFrontLeg.zRot -= (0.13F * freeStruggle) + (0.48F * grip);
        this.rightFrontLeg.zRot += (0.13F * freeStruggle) + (0.48F * grip);

        float kick = Mth.sin(time * 2.6F) * (0.58F + push * 0.32F) * effort;
        this.leftHindLeg.xRot += kick;
        this.rightHindLeg.xRot -= kick;
        this.tail1.yRot += Mth.sin(time * 0.95F) * 0.36F * effort;
        this.tail2.yRot += Mth.sin(time * 1.25F + 0.8F) * 0.52F * effort;
    }
}
