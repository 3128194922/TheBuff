package com.thebuff.api;

import com.thebuff.capability.BuffCapability;
import com.thebuff.capability.BuffHolder;
import com.thebuff.registry.BuffRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class BuffApi {
    private BuffApi() {
    }

    public static Optional<BuffHolder> getHolder(LivingEntity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        return entity.getCapability(BuffCapability.BUFFS).resolve();
    }

    public static boolean apply(
            LivingEntity target,
            ResourceLocation buffId,
            int durationTicks,
            int amplifier,
            @Nullable Entity source
    ) {
        if (target == null || !target.isAlive() || target.level().isClientSide) {
            return false;
        }
        BuffType type = BuffRegistry.get(buffId);
        if (type == null) {
            return false;
        }
        BuffInstance instance = new BuffInstance(
                type,
                amplifier,
                durationTicks,
                source == null ? null : source.getUUID(),
                source == null ? null : source.getDisplayName().getString()
        );
        return getHolder(target).map(holder -> holder.apply(instance, source)).orElse(false);
    }

    public static boolean applyInstance(
            LivingEntity target,
            BuffInstance instance,
            @Nullable Entity source
    ) {
        if (target == null || instance == null || !target.isAlive() || target.level().isClientSide) {
            return false;
        }
        return getHolder(target).map(holder -> holder.apply(instance, source)).orElse(false);
    }

    public static boolean apply(
            LivingEntity target,
            ResourceLocation buffId,
            int durationTicks,
            int amplifier
    ) {
        return apply(target, buffId, durationTicks, amplifier, null);
    }

    public static boolean apply(
            LivingEntity target,
            String buffId,
            int durationTicks,
            int amplifier,
            @Nullable Entity source
    ) {
        return apply(target, BuffRegistry.id(buffId), durationTicks, amplifier, source);
    }

    public static boolean apply(
            LivingEntity target,
            String buffId,
            int durationTicks,
            int amplifier
    ) {
        return apply(target, BuffRegistry.id(buffId), durationTicks, amplifier, null);
    }

    /**
     * Applies a permanent buff. Permanent buffs have no countdown and no expiry check.
     */
    public static boolean applyPermanent(
            LivingEntity target,
            ResourceLocation buffId,
            int amplifier,
            @Nullable Entity source
    ) {
        return apply(target, buffId, -1, amplifier, source);
    }

    public static boolean applyPermanent(
            LivingEntity target,
            ResourceLocation buffId,
            int amplifier
    ) {
        return applyPermanent(target, buffId, amplifier, null);
    }

    public static boolean applyPermanent(
            LivingEntity target,
            String buffId,
            int amplifier,
            @Nullable Entity source
    ) {
        return apply(target, BuffRegistry.id(buffId), -1, amplifier, source);
    }

    public static boolean applyPermanent(
            LivingEntity target,
            String buffId,
            int amplifier
    ) {
        return applyPermanent(target, BuffRegistry.id(buffId), amplifier, null);
    }

    public static boolean remove(LivingEntity target, ResourceLocation buffId) {
        return getHolder(target)
                .map(holder -> holder.remove(buffId, BuffRemovalReason.REMOVED))
                .orElse(false);
    }

    public static boolean remove(LivingEntity target, String buffId) {
        return remove(target, BuffRegistry.id(buffId));
    }

    public static int removeAll(LivingEntity target) {
        return getHolder(target)
                .map(holder -> holder.removeAll(BuffRemovalReason.CLEARED))
                .orElse(0);
    }

    public static int removeBySource(LivingEntity target, Entity source) {
        return removeBySource(target, source == null ? null : source.getUUID());
    }

    public static int removeBySource(LivingEntity target, @Nullable UUID sourceUuid) {
        return getHolder(target)
                .map(holder -> holder.removeBySource(sourceUuid, BuffRemovalReason.REMOVED))
                .orElse(0);
    }

    public static int removeCategory(LivingEntity target, BuffCategory category) {
        return getHolder(target)
                .map(holder -> holder.removeCategory(category, BuffRemovalReason.REMOVED))
                .orElse(0);
    }

    public static boolean setDuration(LivingEntity target, ResourceLocation buffId, int durationTicks) {
        return getHolder(target)
                .map(holder -> holder.setDuration(buffId, durationTicks))
                .orElse(false);
    }

    public static boolean setDuration(LivingEntity target, String buffId, int durationTicks) {
        return setDuration(target, BuffRegistry.id(buffId), durationTicks);
    }

    public static boolean setAmplifier(LivingEntity target, ResourceLocation buffId, int amplifier) {
        return getHolder(target)
                .map(holder -> holder.setAmplifier(buffId, amplifier))
                .orElse(false);
    }

    public static boolean setAmplifier(LivingEntity target, String buffId, int amplifier) {
        return setAmplifier(target, BuffRegistry.id(buffId), amplifier);
    }

    public static boolean refresh(LivingEntity target, ResourceLocation buffId, int durationTicks) {
        return getHolder(target)
                .map(holder -> holder.refresh(buffId, durationTicks))
                .orElse(false);
    }

    public static boolean has(LivingEntity target, ResourceLocation buffId) {
        return getHolder(target).map(holder -> holder.has(buffId)).orElse(false);
    }

    public static boolean has(LivingEntity target, String buffId) {
        return has(target, BuffRegistry.id(buffId));
    }

    public static int getAmplifier(LivingEntity target, ResourceLocation buffId) {
        return getHolder(target)
                .map(holder -> holder.get(buffId))
                .map(BuffInstance::getAmplifier)
                .orElse(-1);
    }

    public static int getAmplifier(LivingEntity target, String buffId) {
        return getAmplifier(target, BuffRegistry.id(buffId));
    }

    public static int getDuration(LivingEntity target, ResourceLocation buffId) {
        return getHolder(target)
                .map(holder -> holder.get(buffId))
                .map(BuffInstance::getDuration)
                .orElse(0);
    }

    public static int getDuration(LivingEntity target, String buffId) {
        return getDuration(target, BuffRegistry.id(buffId));
    }

    public static boolean isPermanent(LivingEntity target, ResourceLocation buffId) {
        return getHolder(target)
                .map(holder -> holder.get(buffId))
                .map(BuffInstance::isPermanent)
                .orElse(false);
    }

    public static boolean isPermanent(LivingEntity target, String buffId) {
        return isPermanent(target, BuffRegistry.id(buffId));
    }

    @Nullable
    public static BuffInstance getInstance(LivingEntity target, ResourceLocation buffId) {
        return getHolder(target).map(holder -> holder.get(buffId)).orElse(null);
    }

    @Nullable
    public static BuffInstance getInstance(LivingEntity target, String buffId) {
        return getInstance(target, BuffRegistry.id(buffId));
    }

    public static List<BuffInstance> getAll(LivingEntity target) {
        return getHolder(target).map(BuffHolder::getBuffs).orElse(List.of());
    }
}
