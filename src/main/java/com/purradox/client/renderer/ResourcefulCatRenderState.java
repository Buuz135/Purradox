package com.purradox.client.renderer;

import com.purradox.cat.CatType;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ResourcefulCatRenderState extends CatRenderState {
    public boolean isTamed;
    public @Nullable Identifier overlay;
    public final List<AttachmentState> attachments = new ArrayList<>();

    public record AttachmentState(CatType.Attachment definition, ItemStackRenderState item) {
    }
}
