package com.purradox.world.level.block.entity;

import com.purradox.registry.ModBlockEntities;
import com.purradox.world.entity.ResourcefulCat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public final class CatSeatBlockEntity extends BlockEntity {
    private static final String ASSIGNED_CAT_TAG = "AssignedCat";
    private @Nullable UUID assignedCat;

    public CatSeatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAT_SEAT.get(), pos, state);
    }

    public boolean isAssignedTo(UUID catId) {
        return catId.equals(this.assignedCat);
    }

    public boolean assign(ResourcefulCat cat) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (this.assignedCat != null && !this.assignedCat.equals(cat.getUUID())) {
            Entity occupant = serverLevel.getEntity(this.assignedCat);
            if (occupant instanceof ResourcefulCat other && other.isAlive() && other.isAssignedToSeat(this.worldPosition)) {
                return false;
            }
        }
        cat.assignSeat(this.worldPosition);
        this.assignedCat = cat.getUUID();
        this.setChanged();
        return true;
    }

    public void clear() {
        if (this.assignedCat == null) {
            return;
        }
        UUID catId = this.assignedCat;
        this.assignedCat = null;
        this.setChanged();
        if (this.level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(catId);
            if (entity instanceof ResourcefulCat cat) {
                cat.clearSeat(this.worldPosition);
            }
        }
    }

    public void clearIfAssigned(UUID catId) {
        if (catId.equals(this.assignedCat)) {
            this.assignedCat = null;
            this.setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String value = input.getStringOr(ASSIGNED_CAT_TAG, "");
        try {
            this.assignedCat = value.isEmpty() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            this.assignedCat = null;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.assignedCat != null) {
            output.putString(ASSIGNED_CAT_TAG, this.assignedCat.toString());
        }
    }
}
