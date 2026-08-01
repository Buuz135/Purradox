package com.purradox.cat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Locale;
import java.util.Optional;


public record CatType(
        String translationKey,
        Optional<Identifier> texture,
        Optional<Identifier> overlay,
        Stats stats,
        Ingredient food,
        float tamingChance,
        List<Attachment> attachments,
        Optional<Production> production,
        Breeding breeding
) {
    private static final Codec<Optional<Identifier>> TEXTURE_CODEC = Codec.STRING.comapFlatMap(
            value -> value.equals("default")
                    ? DataResult.success(Optional.empty())
                    : Identifier.read(value).map(Optional::of),
            value -> value.map(Identifier::toString).orElse("default")
    );

    public static final Codec<CatType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("translation_key").forGetter(CatType::translationKey),
            TEXTURE_CODEC.optionalFieldOf("texture", Optional.empty()).forGetter(CatType::texture),
            Identifier.CODEC.optionalFieldOf("overlay").forGetter(CatType::overlay),
            Stats.CODEC.optionalFieldOf("stats", Stats.DEFAULT).forGetter(CatType::stats),
            Ingredient.CODEC.fieldOf("food").forGetter(CatType::food),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("taming_chance", 1.0F / 3.0F).forGetter(CatType::tamingChance),
            Attachment.CODEC.listOf().optionalFieldOf("attachments", List.of()).forGetter(CatType::attachments),
            Production.CODEC.optionalFieldOf("production").forGetter(CatType::production),
            Breeding.CODEC.optionalFieldOf("breeding", Breeding.DEFAULT).forGetter(CatType::breeding)
    ).apply(instance, CatType::new));

    public record Stats(float maxHealth, float movementSpeed, float attackDamage, float scale) {
        public static final Stats DEFAULT = new Stats(10.0F, 0.3F, 3.0F, 1.0F);
        public static final Codec<Stats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(1.0F, 2048.0F).optionalFieldOf("max_health", DEFAULT.maxHealth).forGetter(Stats::maxHealth),
                Codec.floatRange(0.0F, 16.0F).optionalFieldOf("movement_speed", DEFAULT.movementSpeed).forGetter(Stats::movementSpeed),
                Codec.floatRange(0.0F, 2048.0F).optionalFieldOf("attack_damage", DEFAULT.attackDamage).forGetter(Stats::attackDamage),
                Codec.floatRange(0.0625F, 16.0F).optionalFieldOf("scale", DEFAULT.scale).forGetter(Stats::scale)
        ).apply(instance, Stats::new));
    }

    public record Attachment(
            Identifier model,
            Anchor anchor,
            Transform transform,
            Visibility visibility
    ) {
        public static final Codec<Attachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Attachment::model),
                Anchor.CODEC.optionalFieldOf("anchor", Anchor.BODY).forGetter(Attachment::anchor),
                Transform.CODEC.optionalFieldOf("transform", Transform.IDENTITY).forGetter(Attachment::transform),
                Visibility.CODEC.optionalFieldOf("visible_when", Visibility.ALWAYS).forGetter(Attachment::visibility)
        ).apply(instance, Attachment::new));
    }

    public enum Anchor {
        ROOT("root"),
        HEAD("head"),
        BODY("body"),
        TAIL_1("tail1"),
        TAIL_2("tail2"),
        LEFT_FRONT_LEG("left_front_leg"),
        RIGHT_FRONT_LEG("right_front_leg"),
        LEFT_HIND_LEG("left_hind_leg"),
        RIGHT_HIND_LEG("right_hind_leg");

        public static final Codec<Anchor> CODEC = stringEnumCodec(Anchor.class);
        private final String partName;

        Anchor(String partName) {
            this.partName = partName;
        }

        public String partName() {
            return this.partName;
        }
    }

    public enum Visibility {
        ALWAYS,
        TAMED,
        UNTAMED,
        ADULT,
        BABY,
        SITTING;

        public static final Codec<Visibility> CODEC = stringEnumCodec(Visibility.class);

        public boolean isVisible(boolean tamed, boolean baby, boolean sitting) {
            return switch (this) {
                case ALWAYS -> true;
                case TAMED -> tamed;
                case UNTAMED -> !tamed;
                case ADULT -> !baby;
                case BABY -> baby;
                case SITTING -> sitting;
            };
        }
    }

    public record Transform(Vec3 translation, Vec3 rotation, Vec3 scale) {
        public static final Transform IDENTITY = new Transform(Vec3.ZERO, Vec3.ZERO, Vec3.ONE);
        public static final Codec<Transform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.optionalFieldOf("translation", Vec3.ZERO).forGetter(Transform::translation),
                Vec3.CODEC.optionalFieldOf("rotation", Vec3.ZERO).forGetter(Transform::rotation),
                Vec3.CODEC.optionalFieldOf("scale", Vec3.ONE).forGetter(Transform::scale)
        ).apply(instance, Transform::new));
    }

    public record Vec3(float x, float y, float z) {
        public static final Vec3 ZERO = new Vec3(0.0F, 0.0F, 0.0F);
        public static final Vec3 ONE = new Vec3(1.0F, 1.0F, 1.0F);
        public static final Codec<Vec3> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("x", 0.0F).forGetter(Vec3::x),
                Codec.FLOAT.optionalFieldOf("y", 0.0F).forGetter(Vec3::y),
                Codec.FLOAT.optionalFieldOf("z", 0.0F).forGetter(Vec3::z)
        ).apply(instance, Vec3::new));
    }

    public record Production(
            Interval interval,
            int rolls,
            Conditions conditions,
            List<Output> outputs
    ) {
        private static final Codec<Production> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Interval.CODEC.fieldOf("interval").forGetter(Production::interval),
                Codec.intRange(1, 64).optionalFieldOf("rolls", 1).forGetter(Production::rolls),
                Conditions.CODEC.optionalFieldOf("conditions", Conditions.DEFAULT).forGetter(Production::conditions),
                Output.CODEC.listOf().fieldOf("outputs").forGetter(Production::outputs)
        ).apply(instance, Production::new));

        public static final Codec<Production> CODEC = RAW_CODEC.validate(value ->
                value.outputs.isEmpty()
                        ? DataResult.error(() -> "Production outputs cannot be empty")
                        : DataResult.success(value)
        );

        public int totalWeight() {
            return this.outputs.stream().mapToInt(Output::weight).sum();
        }
    }

    public record Interval(int minTicks, int maxTicks) {
        private static final Codec<Interval> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("min_ticks").forGetter(Interval::minTicks),
                ExtraCodecs.POSITIVE_INT.fieldOf("max_ticks").forGetter(Interval::maxTicks)
        ).apply(instance, Interval::new));

        public static final Codec<Interval> CODEC = RAW_CODEC.validate(value ->
                value.minTicks <= value.maxTicks
                        ? DataResult.success(value)
                        : DataResult.error(() -> "min_ticks must not be greater than max_ticks")
        );
    }

    public record Conditions(
            boolean requiresTamed,
            boolean requiresAdult,
            boolean requiresSitting,
            boolean requiresOnGround,
            List<Identifier> dimensions
    ) {
        public static final Conditions DEFAULT = new Conditions(true, true, false, false, List.of());
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("requires_tamed", DEFAULT.requiresTamed).forGetter(Conditions::requiresTamed),
                Codec.BOOL.optionalFieldOf("requires_adult", DEFAULT.requiresAdult).forGetter(Conditions::requiresAdult),
                Codec.BOOL.optionalFieldOf("requires_sitting", DEFAULT.requiresSitting).forGetter(Conditions::requiresSitting),
                Codec.BOOL.optionalFieldOf("requires_on_ground", DEFAULT.requiresOnGround).forGetter(Conditions::requiresOnGround),
                Identifier.CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(Conditions::dimensions)
        ).apply(instance, Conditions::new));
    }

    public record Output(ProductionStack stack, int weight) {
        public static final Codec<Output> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ProductionStack.CODEC.fieldOf("stack").forGetter(Output::stack),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(Output::weight)
        ).apply(instance, Output::new));
    }

    public record ProductionStack(Holder<Item> item, int count, DataComponentPatch components) {
        public static final Codec<ProductionStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Item.CODEC.fieldOf("id").forGetter(ProductionStack::item),
                ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(ProductionStack::count),
                DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ProductionStack::components)
        ).apply(instance, ProductionStack::new));

        public ItemStack createStack() {
            return new ItemStack(this.item, this.count, this.components);
        }
    }

    public record Breeding(List<Offspring> offspring) {
        public static final Breeding DEFAULT = new Breeding(List.of());
        public static final Codec<Breeding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Offspring.CODEC.listOf().optionalFieldOf("offspring", List.of()).forGetter(Breeding::offspring)
        ).apply(instance, Breeding::new));
    }

    public record Offspring(ResourceKey<CatType> type, int weight) {
        public static final Codec<Offspring> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(CatTypeRegistry.CAT_TYPES).fieldOf("type").forGetter(Offspring::type),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(Offspring::weight)
        ).apply(instance, Offspring::new));
    }

    private static <E extends Enum<E>> Codec<E> stringEnumCodec(Class<E> enumClass) {
        return Codec.STRING.comapFlatMap(
                name -> {
                    try {
                        return DataResult.success(Enum.valueOf(enumClass, name.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown " + enumClass.getSimpleName() + " value: " + name);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }
}
