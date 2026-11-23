package com.thebuff.client;

import com.thebuff.api.BuffCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record ClientBuff(
        ResourceLocation id,
        Component name,
        int color,
        BuffCategory category,
        int amplifier,
        long expireAt,
        boolean permanent,
        boolean showInHud
) {
    public int remaining(long gameTime) {
        if (permanent) {
            return -1;
        }
        return (int) Math.max(0L, expireAt - gameTime);
    }
}
