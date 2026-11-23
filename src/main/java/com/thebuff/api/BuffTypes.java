package com.thebuff.api;

import com.thebuff.TheBuff;
import com.thebuff.registry.BuffRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.RegistryObject;

public final class BuffTypes {
    public static final RegistryObject<BuffType> TEST_BURN = BuffRegistry.BUFFS.register("test_burn", () ->
            BuffType.builder(new ResourceLocation(TheBuff.MODID, "test_burn"))
                    .name(Component.translatable("buff.thebuff.test_burn"))
                    .color(0xFF6600)
                    .category(BuffCategory.HARMFUL)
                    .damage(1.0D, 20)
                    .damageType(new ResourceLocation(TheBuff.MODID, "buff"))
                    .build());

    public static final RegistryObject<BuffType> TEST_SWIFTNESS = BuffRegistry.BUFFS.register("test_swiftness", () ->
            BuffType.builder(new ResourceLocation(TheBuff.MODID, "test_swiftness"))
                    .name(Component.translatable("buff.thebuff.test_swiftness"))
                    .color(0x66CCFF)
                    .category(BuffCategory.BENEFICIAL)
                    .attribute(Attributes.MOVEMENT_SPEED, 0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .build());

    private BuffTypes() {
    }

    public static void init() {
        // Forces static initialization of the registry objects.
    }
}
