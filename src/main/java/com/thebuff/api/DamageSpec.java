package com.thebuff.api;

import com.thebuff.TheBuff;
import net.minecraft.resources.ResourceLocation;

public record DamageSpec(
        double amountPerLevel,
        int intervalTicks,
        ResourceLocation damageType,
        boolean ignoreInvulnerability,
        boolean applyImmediately
) {
    public static final ResourceLocation DEFAULT_DAMAGE_TYPE = new ResourceLocation(TheBuff.MODID, "buff");

    public DamageSpec {
        if (amountPerLevel < 0.0D) {
            throw new IllegalArgumentException("amountPerLevel must be >= 0");
        }
        if (intervalTicks < 0) {
            throw new IllegalArgumentException("intervalTicks must be >= 0");
        }
        if (damageType == null) {
            damageType = DEFAULT_DAMAGE_TYPE;
        }
    }

    public boolean enabled() {
        return amountPerLevel > 0.0D && intervalTicks > 0;
    }
}
