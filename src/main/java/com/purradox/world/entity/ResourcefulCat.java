package com.purradox.world.entity;

import com.purradox.cat.CatType;
import com.purradox.cat.CatTypeRegistry;
import com.purradox.registry.ModEntities;
import com.purradox.world.item.CatTeaserWandItem;
import com.purradox.world.level.block.entity.CatSeatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.EnumSet;
import java.util.Optional;

public final class ResourcefulCat extends Cat {
    private static final String CAT_TYPE_TAG = "CatType";
    private static final String PRODUCTION_TIME_TAG = "ProductionTime";
    private static final String ASSIGNED_SEAT_TAG = "AssignedSeat";
    private static final EntityDataAccessor<String> DATA_CAT_TYPE = SynchedEntityData.defineId(
            ResourcefulCat.class,
            EntityDataSerializers.STRING
    );

    private int productionTime = -1;
    private CatType.@Nullable Stats appliedStats;
    private @Nullable BlockPos assignedSeat;

    public ResourcefulCat(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AssignedSeatGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 0.6, this::isFood, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_CAT_TYPE, CatTypeRegistry.DEFAULT.identifier().toString());
    }

    public ResourceKey<CatType> getCatTypeKey() {
        Identifier id = Identifier.tryParse(this.entityData.get(DATA_CAT_TYPE));
        return CatTypeRegistry.key(id == null ? CatTypeRegistry.DEFAULT.identifier() : id);
    }

    public Optional<Holder.Reference<CatType>> getCatType() {
        Registry<CatType> registry = this.registryAccess().lookupOrThrow(CatTypeRegistry.CAT_TYPES);
        return registry.get(this.getCatTypeKey()).or(registry::getAny);
    }

    public void setCatType(ResourceKey<CatType> type, boolean healToFull) {
        this.entityData.set(DATA_CAT_TYPE, type.identifier().toString());
        this.productionTime = -1;
        this.applyConfiguredAttributes(healToFull);
    }

    @Override
    protected Component getTypeName() {
        return this.getCatType()
                .<Component>map(holder -> Component.translatable(holder.value().translationKey()))
                .orElseGet(super::getTypeName);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return this.getCatType().map(holder -> holder.value().food().test(stack)).orElseGet(() -> super.isFood(stack));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof CatTeaserWandItem wand) {
            return wand.interactLivingEntity(stack, player, this, hand);
        }
        if (!this.isTame() && this.isFood(stack)) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem(player, hand, stack);
                float chance = this.getCatType().map(holder -> holder.value().tamingChance()).orElse(1.0F / 3.0F);
                if (this.random.nextFloat() < chance && !EventHooks.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                this.setPersistenceRequired();
                this.playEatingSound();
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.isAlive()) {
            this.validateSeatAssignment(serverLevel);
            this.refreshConfiguredAttributes();
            this.tickProduction(serverLevel);
        }
    }

    public void assignSeat(BlockPos seatPos) {
        if (this.assignedSeat != null && !this.assignedSeat.equals(seatPos)
                && this.level().getBlockEntity(this.assignedSeat) instanceof CatSeatBlockEntity oldSeat) {
            oldSeat.clearIfAssigned(this.getUUID());
        }
        this.assignedSeat = seatPos.immutable();
        this.setOrderedToSit(false);
        this.setInSittingPose(false);
        this.getNavigation().stop();
        this.setPersistenceRequired();
    }

    public boolean isAssignedToSeat(BlockPos seatPos) {
        return seatPos.equals(this.assignedSeat);
    }

    public void clearSeat(BlockPos seatPos) {
        if (seatPos.equals(this.assignedSeat)) {
            this.clearSeatLocally();
        }
    }

    private void validateSeatAssignment(ServerLevel level) {
        if (this.assignedSeat != null && level.hasChunkAt(this.assignedSeat) && !this.hasValidAssignedSeat()) {
            this.clearSeatLocally();
        }
    }

    private boolean hasValidAssignedSeat() {
        return this.assignedSeat != null
                && this.level().getBlockEntity(this.assignedSeat) instanceof CatSeatBlockEntity seat
                && seat.isAssignedTo(this.getUUID());
    }

    private void clearSeatLocally() {
        this.assignedSeat = null;
        this.setOrderedToSit(false);
        this.setInSittingPose(false);
        this.getNavigation().stop();
    }

    private void refreshConfiguredAttributes() {
        this.getCatType().ifPresent(holder -> {
            CatType.Stats currentStats = holder.value().stats();
            if (currentStats != this.appliedStats) {
                this.productionTime = -1;
                this.applyConfiguredAttributes(false);
            }
        });
    }

    private void tickProduction(ServerLevel level) {
        Optional<CatType> type = this.getCatType().map(Holder::value);
        if (type.isEmpty() || type.get().production().isEmpty()) {
            this.productionTime = -1;
            return;
        }

        CatType.Production production = type.get().production().get();
        if (this.productionTime < 0) {
            this.resetProductionTime(production);
        }
        if (!this.canProduce(production.conditions(), level)) {
            return;
        }
        if (--this.productionTime > 0) {
            return;
        }

        for (int roll = 0; roll < production.rolls(); roll++) {
            CatType.Output output = pickWeighted(production.outputs(), production.totalWeight());
            if (output != null) {
                this.spawnAtLocation(level, output.stack().createStack(), 0.25F);
            }
        }
        this.resetProductionTime(production);
    }

    private boolean canProduce(CatType.Conditions conditions, ServerLevel level) {
        if (conditions.requiresTamed() && !this.isTame()) {
            return false;
        }
        if (conditions.requiresAdult() && this.isBaby()) {
            return false;
        }
        if (conditions.requiresSitting() && !this.isInSittingPose()) {
            return false;
        }
        if (conditions.requiresOnGround() && !this.onGround()) {
            return false;
        }
        return conditions.dimensions().isEmpty() || conditions.dimensions().contains(level.dimension().identifier());
    }

    private void resetProductionTime(CatType.Production production) {
        CatType.Interval interval = production.interval();
        this.productionTime = Mth.nextInt(this.random, interval.minTicks(), interval.maxTicks());
    }

    private CatType.@Nullable Output pickWeighted(List<CatType.Output> outputs, int totalWeight) {
        if (totalWeight <= 0) {
            return null;
        }
        int choice = this.random.nextInt(totalWeight);
        for (CatType.Output output : outputs) {
            choice -= output.weight();
            if (choice < 0) {
                return output;
            }
        }
        return outputs.getLast();
    }

    @Override
    public @Nullable ResourcefulCat getBreedOffspring(ServerLevel level, AgeableMob partner) {
        ResourcefulCat baby = ModEntities.CAT.get().create(level, EntitySpawnReason.BREEDING);
        if (baby == null) {
            return null;
        }

        ResourceKey<CatType> childType = this.pickOffspringType(partner);
        baby.setCatType(childType, true);
        if (partner instanceof Cat partnerCat) {
            baby.setComponent(
                    DataComponents.CAT_VARIANT,
                    this.random.nextBoolean() ? this.getVariant() : partnerCat.getVariant()
            );
        }
        if (this.isTame()) {
            baby.setOwnerReference(this.getOwnerReference());
            baby.setTame(true, true);
        }
        return baby;
    }

    private ResourceKey<CatType> pickOffspringType(AgeableMob partner) {
        List<CatType.Offspring> configured = this.getCatType()
                .map(holder -> holder.value().breeding().offspring())
                .orElse(List.of());
        if (!configured.isEmpty()) {
            int total = configured.stream().mapToInt(CatType.Offspring::weight).sum();
            int choice = this.random.nextInt(total);
            for (CatType.Offspring offspring : configured) {
                choice -= offspring.weight();
                if (choice < 0) {
                    return offspring.type();
                }
            }
        }
        if (partner instanceof ResourcefulCat other && this.random.nextBoolean()) {
            return other.getCatTypeKey();
        }
        return this.getCatTypeKey();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(CAT_TYPE_TAG, this.getCatTypeKey().identifier().toString());
        output.putInt(PRODUCTION_TIME_TAG, this.productionTime);
        if (this.assignedSeat != null) {
            output.putLong(ASSIGNED_SEAT_TAG, this.assignedSeat.asLong());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        Identifier id = Identifier.tryParse(input.getStringOr(CAT_TYPE_TAG, CatTypeRegistry.DEFAULT.identifier().toString()));
        this.entityData.set(DATA_CAT_TYPE, (id == null ? CatTypeRegistry.DEFAULT.identifier() : id).toString());
        this.productionTime = input.getIntOr(PRODUCTION_TIME_TAG, -1);
        long seatPos = input.getLongOr(ASSIGNED_SEAT_TAG, Long.MIN_VALUE);
        this.assignedSeat = seatPos == Long.MIN_VALUE ? null : BlockPos.of(seatPos);
        this.applyConfiguredAttributes(false);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            EntitySpawnReason reason,
            @Nullable SpawnGroupData spawnData
    ) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.applyConfiguredAttributes(true);
        return result;
    }

    private void applyConfiguredAttributes(boolean healToFull) {
        this.getCatType().ifPresent(holder -> {
            CatType.Stats stats = holder.value().stats();
            setBaseValue(Attributes.MAX_HEALTH, stats.maxHealth());
            setBaseValue(Attributes.MOVEMENT_SPEED, stats.movementSpeed());
            setBaseValue(Attributes.ATTACK_DAMAGE, stats.attackDamage());
            setBaseValue(Attributes.SCALE, stats.scale());
            if (healToFull) {
                this.setHealth(this.getMaxHealth());
            } else if (this.getHealth() > this.getMaxHealth()) {
                this.setHealth(this.getMaxHealth());
            }
            this.appliedStats = stats;
        });
    }

    private void setBaseValue(Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private static final class AssignedSeatGoal extends Goal {
        private static final double ARRIVAL_DISTANCE_SQUARED = 0.25;
        private static final double SEAT_HEIGHT = 5.0 / 16.0;
        private final ResourcefulCat cat;

        private AssignedSeatGoal(ResourcefulCat cat) {
            this.cat = cat;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.cat.hasValidAssignedSeat();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.cat.setOrderedToSit(false);
            this.cat.setInSittingPose(false);
            this.moveToSeat();
        }

        @Override
        public void stop() {
            this.cat.setInSittingPose(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            BlockPos seatPos = this.cat.assignedSeat;
            if (seatPos == null) {
                return;
            }
            double targetX = seatPos.getX() + 0.5;
            double targetZ = seatPos.getZ() + 0.5;
            double deltaX = this.cat.getX() - targetX;
            double deltaZ = this.cat.getZ() - targetZ;
            if (deltaX * deltaX + deltaZ * deltaZ <= ARRIVAL_DISTANCE_SQUARED
                    && Math.abs(this.cat.getY() - seatPos.getY()) < 1.5) {
                this.cat.getNavigation().stop();
                this.cat.setPos(targetX, seatPos.getY() + SEAT_HEIGHT, targetZ);
                this.cat.setDeltaMovement(0.0, 0.0, 0.0);
                this.cat.setYRot(180.0F);
                this.cat.setYBodyRot(180.0F);
                this.cat.setYHeadRot(180.0F);
                this.cat.setInSittingPose(true);
            } else {
                this.cat.setInSittingPose(false);
                if (this.cat.tickCount % 20 == 0 || this.cat.getNavigation().isDone()) {
                    this.moveToSeat();
                }
            }
        }

        private void moveToSeat() {
            BlockPos seatPos = this.cat.assignedSeat;
            if (seatPos != null) {
                this.cat.getNavigation().moveTo(
                        seatPos.getX() + 0.5,
                        seatPos.getY() + 1.0,
                        seatPos.getZ() + 0.5,
                        1.1
                );
            }
        }
    }
}
