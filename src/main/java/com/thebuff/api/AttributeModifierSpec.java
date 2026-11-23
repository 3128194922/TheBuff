package com.thebuff.api;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeModifierSpec(Attribute attribute, double amountPerLevel, AttributeModifier.Operation operation) {
    public AttributeModifierSpec {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("operation must not be null");
        }
    }
}
