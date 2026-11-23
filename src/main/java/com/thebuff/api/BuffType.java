package com.thebuff.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BuffType {
    private final ResourceLocation id;
    private final Component displayName;
    private final int color;
    private final BuffCategory category;
    private final int maxAmplifier;
    private final List<AttributeModifierSpec> attributeModifiers;
    @Nullable
    private final DamageSpec damage;
    @Nullable
    private final BuffTickHandler onApply;
    @Nullable
    private final BuffTickHandler onTick;
    @Nullable
    private final BuffTickHandler onExpire;
    private final boolean showInHud;

    private BuffType(
            ResourceLocation id,
            Component displayName,
            int color,
            BuffCategory category,
            int maxAmplifier,
            List<AttributeModifierSpec> attributeModifiers,
            @Nullable DamageSpec damage,
            @Nullable BuffTickHandler onApply,
            @Nullable BuffTickHandler onTick,
            @Nullable BuffTickHandler onExpire,
            boolean showInHud
    ) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.category = category;
        this.maxAmplifier = maxAmplifier;
        this.attributeModifiers = List.copyOf(attributeModifiers);
        this.damage = damage;
        this.onApply = onApply;
        this.onTick = onTick;
        this.onExpire = onExpire;
        this.showInHud = showInHud;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public ResourceLocation getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public BuffCategory getCategory() {
        return category;
    }

    public int getMaxAmplifier() {
        return maxAmplifier;
    }

    public List<AttributeModifierSpec> getAttributeModifiers() {
        return attributeModifiers;
    }

    @Nullable
    public DamageSpec getDamage() {
        return damage;
    }

    @Nullable
    public BuffTickHandler getOnApply() {
        return onApply;
    }

    @Nullable
    public BuffTickHandler getOnTick() {
        return onTick;
    }

    @Nullable
    public BuffTickHandler getOnExpire() {
        return onExpire;
    }

    public boolean isShowInHud() {
        return showInHud;
    }

    public boolean needsTick() {
        return onTick != null || (damage != null && damage.enabled());
    }

    public static final class Builder {
        private final ResourceLocation id;
        private Component displayName;
        private int color = 0xFFFFFF;
        private BuffCategory category = BuffCategory.NEUTRAL;
        private int maxAmplifier = 255;
        private final List<AttributeModifierSpec> attributeModifiers = new ArrayList<>();
        private double damageAmountPerLevel = 0.0D;
        private int damageIntervalTicks = 0;
        private ResourceLocation damageType = DamageSpec.DEFAULT_DAMAGE_TYPE;
        private boolean ignoreInvulnerability = true;
        private boolean applyImmediately = true;
        private BuffTickHandler onApply;
        private BuffTickHandler onTick;
        private BuffTickHandler onExpire;
        private boolean showInHud = true;

        private Builder(ResourceLocation id) {
            this.id = Objects.requireNonNull(id, "id");
            this.displayName = Component.translatable("buff." + id.getNamespace() + "." + id.getPath());
        }

        public Builder name(String name) {
            this.displayName = Component.literal(name);
            return this;
        }

        public Builder name(Component name) {
            this.displayName = Objects.requireNonNull(name, "name");
            return this;
        }

        public Builder color(int color) {
            this.color = color & 0xFFFFFF;
            return this;
        }

        public Builder category(BuffCategory category) {
            this.category = Objects.requireNonNull(category, "category");
            return this;
        }

        public Builder category(String category) {
            return category(BuffCategory.byName(category));
        }

        public Builder maxLevel(int level) {
            if (level < 1) {
                throw new IllegalArgumentException("maxLevel must be >= 1");
            }
            this.maxAmplifier = level - 1;
            return this;
        }

        public Builder attribute(Attribute attribute, double amountPerLevel) {
            return attribute(attribute, amountPerLevel, AttributeModifier.Operation.ADDITION);
        }

        public Builder attribute(Attribute attribute, double amountPerLevel, AttributeModifier.Operation operation) {
            this.attributeModifiers.add(new AttributeModifierSpec(attribute, amountPerLevel, operation));
            return this;
        }

        public Builder damage(double amountPerLevel, int intervalTicks) {
            this.damageAmountPerLevel = amountPerLevel;
            this.damageIntervalTicks = intervalTicks;
            return this;
        }

        public Builder damageType(ResourceLocation damageType) {
            this.damageType = Objects.requireNonNull(damageType, "damageType");
            return this;
        }

        public Builder ignoreInvulnerability(boolean ignore) {
            this.ignoreInvulnerability = ignore;
            return this;
        }

        public Builder applyImmediately(boolean applyImmediately) {
            this.applyImmediately = applyImmediately;
            return this;
        }

        public Builder onApply(BuffTickHandler handler) {
            this.onApply = handler;
            return this;
        }

        public Builder onTick(BuffTickHandler handler) {
            this.onTick = handler;
            return this;
        }

        public Builder onExpire(BuffTickHandler handler) {
            this.onExpire = handler;
            return this;
        }

        public Builder showInHud(boolean showInHud) {
            this.showInHud = showInHud;
            return this;
        }

        public BuffType build() {
            DamageSpec damage = damageIntervalTicks > 0 && damageAmountPerLevel > 0.0D
                    ? new DamageSpec(damageAmountPerLevel, damageIntervalTicks, damageType, ignoreInvulnerability, applyImmediately)
                    : null;
            return new BuffType(
                    id,
                    displayName,
                    color,
                    category,
                    maxAmplifier,
                    attributeModifiers,
                    damage,
                    onApply,
                    onTick,
                    onExpire,
                    showInHud
            );
        }
    }
}
