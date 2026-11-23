package com.thebuff.api;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class BuffDamageSource extends DamageSource {
    @Nullable
    private final String sourceName;

    public BuffDamageSource(
            Holder<DamageType> type,
            @Nullable Entity directEntity,
            @Nullable Entity causingEntity,
            @Nullable String sourceName
    ) {
        super(type, directEntity, causingEntity);
        this.sourceName = sourceName;
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity entity) {
        String msgId = getMsgId();
        if (!msgId.startsWith("thebuff.")) {
            return super.getLocalizedDeathMessage(entity);
        }

        Entity source = getEntity();
        if (source != null) {
            return Component.translatable("death.attack." + msgId + ".player", entity.getDisplayName(), source.getDisplayName());
        }
        if (sourceName != null && !sourceName.isBlank()) {
            return Component.translatable("death.attack." + msgId + ".source", entity.getDisplayName(), sourceName);
        }
        return Component.translatable("death.attack." + msgId, entity.getDisplayName());
    }
}
