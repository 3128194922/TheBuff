package com.thebuff.compat.kubejs;

import com.thebuff.api.AttributeModifierSpec;
import com.thebuff.api.BuffCategory;
import com.thebuff.api.BuffTickHandler;
import com.thebuff.api.BuffType;
import com.thebuff.api.DamageSpec;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.BaseFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BuffBuilder extends BuilderBase<BuffType> {
    private final List<AttributeModifierSpec> attributes = new ArrayList<>();
    private int color = 0xFFFFFF;
    private BuffCategory category = BuffCategory.NEUTRAL;
    private int maxAmplifier = 255;
    private double damageAmount = 0.0D;
    private int damageInterval = 0;
    private ResourceLocation damageType = DamageSpec.DEFAULT_DAMAGE_TYPE;
    private boolean ignoreInvulnerability = true;
    private boolean applyImmediately = true;
    private boolean showInHud = true;
    @Nullable
    private BuffTickHandler onApply;
    @Nullable
    private BuffTickHandler onTick;
    @Nullable
    private BuffTickHandler onExpire;

    public BuffBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public RegistryInfo<BuffType> getRegistryType() {
        return TheBuffKubeJSPlugin.info();
    }

    public BuffBuilder name(String name) {
        this.displayName = Component.literal(name);
        return this;
    }

    public BuffBuilder color(int color) {
        this.color = color & 0xFFFFFF;
        return this;
    }

    public BuffBuilder color(String color) {
        String value = color.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }
        if (!value.matches("[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Buff " + id + " color must be a 6-digit hex color");
        }
        return color(Integer.parseInt(value, 16));
    }

    public BuffBuilder category(String category) {
        this.category = BuffCategory.byName(category);
        return this;
    }

    public BuffBuilder maxLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Buff " + id + " maxLevel must be >= 1");
        }
        this.maxAmplifier = level - 1;
        return this;
    }

    public BuffBuilder attribute(String attributeId, double amountPerLevel) {
        return attribute(attributeId, amountPerLevel, "addition");
    }

    public BuffBuilder attribute(String attributeId, double amountPerLevel, String operation) {
        ResourceLocation location = ResourceLocation.tryParse(attributeId);
        if (location == null) {
            throw new IllegalArgumentException("Buff " + id + " has invalid attribute id: " + attributeId);
        }
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(location);
        if (attribute == null) {
            throw new IllegalArgumentException("Buff " + id + " has unknown attribute: " + attributeId);
        }
        attributes.add(new AttributeModifierSpec(attribute, amountPerLevel, parseOperation(operation)));
        return this;
    }

    public BuffBuilder damage(double amountPerLevel, int intervalTicks) {
        this.damageAmount = amountPerLevel;
        this.damageInterval = intervalTicks;
        return this;
    }

    public BuffBuilder damageType(String damageType) {
        ResourceLocation location = ResourceLocation.tryParse(damageType);
        if (location == null) {
            throw new IllegalArgumentException("Buff " + id + " has invalid damage type: " + damageType);
        }
        this.damageType = location;
        return this;
    }

    public BuffBuilder ignoreInvulnerability(boolean ignore) {
        this.ignoreInvulnerability = ignore;
        return this;
    }

    public BuffBuilder applyImmediately(boolean applyImmediately) {
        this.applyImmediately = applyImmediately;
        return this;
    }

    public BuffBuilder onApply(Object callback) {
        this.onApply = handler(callback, "onApply");
        return this;
    }

    public BuffBuilder onTick(Object callback) {
        this.onTick = handler(callback, "onTick");
        return this;
    }

    public BuffBuilder onExpire(Object callback) {
        this.onExpire = handler(callback, "onExpire");
        return this;
    }

    public BuffBuilder showInHud(boolean showInHud) {
        this.showInHud = showInHud;
        return this;
    }

    @Override
    public BuffType createObject() {
        Component display = displayName != null ? displayName : Component.translatable(getBuilderTranslationKey());
        BuffType.Builder builder = BuffType.builder(id)
                .name(display)
                .color(color)
                .category(category)
                .maxLevel(maxAmplifier + 1)
                .showInHud(showInHud);

        for (AttributeModifierSpec spec : attributes) {
            builder.attribute(spec.attribute(), spec.amountPerLevel(), spec.operation());
        }

        if (damageAmount > 0.0D && damageInterval > 0) {
            builder.damage(damageAmount, damageInterval)
                    .damageType(damageType)
                    .ignoreInvulnerability(ignoreInvulnerability)
                    .applyImmediately(applyImmediately);
        }

        if (onApply != null) {
            builder.onApply(onApply);
        }
        if (onTick != null) {
            builder.onTick(onTick);
        }
        if (onExpire != null) {
            builder.onExpire(onExpire);
        }

        return builder.build();
    }

    private AttributeModifier.Operation parseOperation(String operation) {
        String normalized = operation == null ? "addition" : operation.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "addition", "add" -> AttributeModifier.Operation.ADDITION;
            case "multiply_base", "base" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total", "total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> throw new IllegalArgumentException(
                    "Buff " + id + " attribute operation must be addition, multiply_base or multiply_total");
        };
    }

    @Nullable
    private BuffTickHandler handler(Object callback, String name) {
        if (callback == null) {
            return null;
        }
        if (callback instanceof BaseFunction function) {
            return UtilsJS.makeFunctionProxy(ScriptType.STARTUP, BuffTickHandler.class, function);
        }
        throw new IllegalArgumentException("Buff " + id + " " + name + " must be a function");
    }
}
