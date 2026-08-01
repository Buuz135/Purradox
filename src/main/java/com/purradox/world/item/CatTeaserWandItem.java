package com.purradox.world.item;

import com.purradox.world.entity.ResourcefulCat;
import com.purradox.world.level.block.entity.CatSeatBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.UUID;

public final class CatTeaserWandItem extends Item {
    private static final String SELECTED_CAT_TAG = "SelectedCat";

    public CatTeaserWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof ResourcefulCat cat)) {
            return InteractionResult.PASS;
        }
        /*
        if (!cat.isTame() || !cat.isOwnedBy(player)) {
            if (!player.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.purradox.cat_teaser_wand.not_owned"));
            }
            return InteractionResult.FAIL;
        }*/
        if (!player.level().isClientSide()) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(SELECTED_CAT_TAG, cat.getUUID().toString()));
            player.sendOverlayMessage(
                    Component.translatable("message.purradox.cat_teaser_wand.selected", cat.getDisplayName())
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof CatSeatBlockEntity seat)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.FAIL;
        }
        if (context.isSecondaryUseActive()) {
            seat.clear();
            player.sendOverlayMessage(Component.translatable("message.purradox.cat_teaser_wand.cleared"));
            return InteractionResult.SUCCESS;
        }

        UUID selectedCat = getSelectedCat(context.getItemInHand());
        if (selectedCat == null) {
            player.sendOverlayMessage(Component.translatable("message.purradox.cat_teaser_wand.no_cat"));
            return InteractionResult.FAIL;
        }
        Entity entity = serverLevel.getEntity(selectedCat);
        if (!(entity instanceof ResourcefulCat cat) || !cat.isAlive() /*|| !cat.isOwnedBy(player)*/) {
            player.sendOverlayMessage(Component.translatable("message.purradox.cat_teaser_wand.cat_unavailable"));
            return InteractionResult.FAIL;
        }
        if (!seat.assign(cat)) {
            player.sendOverlayMessage(Component.translatable("message.purradox.cat_teaser_wand.seat_occupied"));
            return InteractionResult.FAIL;
        }
        player.sendOverlayMessage(
                Component.translatable("message.purradox.cat_teaser_wand.assigned", cat.getDisplayName())
        );
        return InteractionResult.SUCCESS;
    }

    private static UUID getSelectedCat(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String value = tag.getStringOr(SELECTED_CAT_TAG, "");
        try {
            return value.isEmpty() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
