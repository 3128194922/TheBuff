package com.thebuff.network;

import com.thebuff.api.BuffCategory;
import com.thebuff.api.BuffInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record BuffSnapshot(
        ResourceLocation id,
        Component name,
        int color,
        BuffCategory category,
        int amplifier,
        int duration,
        boolean showInHud
) {
    public static BuffSnapshot of(BuffInstance instance) {
        return new BuffSnapshot(
                instance.getId(),
                instance.getType().getDisplayName(),
                instance.getType().getColor(),
                instance.getType().getCategory(),
                instance.getAmplifier(),
                instance.getDuration(),
                instance.getType().isShowInHud()
        );
    }

    public boolean permanent() {
        return duration < 0;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeComponent(name);
        buf.writeInt(color);
        buf.writeByte(category.ordinal());
        buf.writeVarInt(amplifier);
        buf.writeVarInt(duration);
        buf.writeBoolean(showInHud);
    }

    public static BuffSnapshot decode(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component name = buf.readComponent();
        int color = buf.readInt();
        int categoryIndex = buf.readByte();
        BuffCategory[] categories = BuffCategory.values();
        BuffCategory category = categories[Math.max(0, Math.min(categoryIndex, categories.length - 1))];
        int amplifier = buf.readVarInt();
        int duration = buf.readVarInt();
        boolean showInHud = buf.readBoolean();
        return new BuffSnapshot(id, name, color, category, amplifier, duration, showInHud);
    }
}
