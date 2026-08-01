package com.purradox.registry;

import com.purradox.Purradox;
import com.purradox.world.entity.ResourcefulCat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(Purradox.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ResourcefulCat>> CAT = ENTITY_TYPES.registerEntityType(
            "time_traveling_cat",
            ResourcefulCat::new,
            MobCategory.CREATURE,
            builder -> builder
                    .sized(0.6F, 0.7F)
                    .eyeHeight(0.35F)
                    .passengerAttachments(0.5125F)
                    .clientTrackingRange(8)
    );

    private ModEntities() {
    }
}
