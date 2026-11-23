package com.thebuff.compat.kubejs;

import com.thebuff.api.BuffApi;
import com.thebuff.api.BuffCategory;
import com.thebuff.api.BuffInstance;
import com.thebuff.registry.BuffRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class TheBuffBindings {
    public static final TheBuffBindings INSTANCE = new TheBuffBindings();

    private TheBuffBindings() {
    }

    public boolean apply(LivingEntity target, String buffId, int durationTicks, int amplifier, @Nullable Entity source) {
        return BuffApi.apply(target, BuffRegistry.id(buffId), durationTicks, amplifier, source);
    }

    public boolean apply(LivingEntity target, String buffId, int durationTicks, int amplifier) {
        return apply(target, buffId, durationTicks, amplifier, null);
    }

    public boolean apply(LivingEntity target, ResourceLocation buffId, int durationTicks, int amplifier, @Nullable Entity source) {
        return BuffApi.apply(target, buffId, durationTicks, amplifier, source);
    }

    public boolean applyPermanent(LivingEntity target, String buffId, int amplifier, @Nullable Entity source) {
        return BuffApi.applyPermanent(target, BuffRegistry.id(buffId), amplifier, source);
    }

    public boolean applyPermanent(LivingEntity target, String buffId, int amplifier) {
        return applyPermanent(target, buffId, amplifier, null);
    }

    public boolean remove(LivingEntity target, String buffId) {
        return BuffApi.remove(target, BuffRegistry.id(buffId));
    }

    public int removeAll(LivingEntity target) {
        return BuffApi.removeAll(target);
    }

    public int removeBySource(LivingEntity target, Entity source) {
        return BuffApi.removeBySource(target, source);
    }

    public int removeBySource(LivingEntity target, @Nullable UUID sourceUuid) {
        return BuffApi.removeBySource(target, sourceUuid);
    }

    public int removeCategory(LivingEntity target, String category) {
        return BuffApi.removeCategory(target, BuffCategory.byName(category));
    }

    public boolean setDuration(LivingEntity target, String buffId, int durationTicks) {
        return BuffApi.setDuration(target, BuffRegistry.id(buffId), durationTicks);
    }

    public boolean setAmplifier(LivingEntity target, String buffId, int amplifier) {
        return BuffApi.setAmplifier(target, BuffRegistry.id(buffId), amplifier);
    }

    public boolean refresh(LivingEntity target, String buffId, int durationTicks) {
        return BuffApi.refresh(target, BuffRegistry.id(buffId), durationTicks);
    }

    public boolean has(LivingEntity target, String buffId) {
        return BuffApi.has(target, BuffRegistry.id(buffId));
    }

    public int getLevel(LivingEntity target, String buffId) {
        return BuffApi.getAmplifier(target, BuffRegistry.id(buffId));
    }

    public int getAmplifier(LivingEntity target, String buffId) {
        return getLevel(target, buffId);
    }

    public int getDuration(LivingEntity target, String buffId) {
        return BuffApi.getDuration(target, BuffRegistry.id(buffId));
    }

    public boolean isPermanent(LivingEntity target, String buffId) {
        return BuffApi.isPermanent(target, BuffRegistry.id(buffId));
    }

    @Nullable
    public BuffInstance getInstance(LivingEntity target, String buffId) {
        return BuffApi.getInstance(target, BuffRegistry.id(buffId));
    }

    public List<BuffInstance> getAll(LivingEntity target) {
        return BuffApi.getAll(target);
    }
}
