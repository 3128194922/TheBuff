package com.thebuff.api;

import com.thebuff.registry.BuffRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class BuffInstance {
    private final BuffType type;
    private int amplifier;
    private int duration;
    private long elapsedTicks;
    private int damageCounter;
    @Nullable
    private UUID sourceUuid;
    @Nullable
    private String sourceName;
    private CompoundTag extra;

    public BuffInstance(BuffType type, int amplifier, int duration, @Nullable UUID sourceUuid, @Nullable String sourceName) {
        this.type = type;
        this.amplifier = Math.max(0, Math.min(amplifier, type.getMaxAmplifier()));
        this.duration = normalizeDuration(duration);
        this.sourceUuid = sourceUuid;
        this.sourceName = sourceName;
        this.extra = new CompoundTag();
    }

    private BuffInstance(BuffType type) {
        this.type = type;
        this.extra = new CompoundTag();
    }

    public static int normalizeDuration(int duration) {
        return duration < 0 ? -1 : duration;
    }

    public BuffType getType() {
        return type;
    }

    public ResourceLocation getId() {
        return type.getId();
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = Math.max(0, Math.min(amplifier, type.getMaxAmplifier()));
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = normalizeDuration(duration);
    }

    public boolean isPermanent() {
        return duration < 0;
    }

    public long getElapsedTicks() {
        return elapsedTicks;
    }

    public void tickElapsed() {
        elapsedTicks++;
    }

    public int getDamageCounter() {
        return damageCounter;
    }

    public void setDamageCounter(int damageCounter) {
        this.damageCounter = Math.max(0, damageCounter);
    }

    @Nullable
    public UUID getSourceUuid() {
        return sourceUuid;
    }

    public void setSource(@Nullable UUID sourceUuid, @Nullable String sourceName) {
        this.sourceUuid = sourceUuid;
        this.sourceName = sourceName;
    }

    @Nullable
    public String getSourceName() {
        return sourceName;
    }

    public CompoundTag getExtra() {
        return extra;
    }

    public void setExtra(CompoundTag extra) {
        this.extra = extra == null ? new CompoundTag() : extra.copy();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", type.getId().toString());
        tag.putInt("Amplifier", amplifier);
        tag.putInt("Duration", duration);
        tag.putLong("Elapsed", elapsedTicks);
        tag.putInt("DamageCounter", damageCounter);
        if (sourceUuid != null) {
            tag.putUUID("Source", sourceUuid);
        }
        if (sourceName != null) {
            tag.putString("SourceName", sourceName);
        }
        if (!extra.isEmpty()) {
            tag.put("Extra", extra.copy());
        }
        return tag;
    }

    @Nullable
    public static BuffInstance load(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("Id"));
        if (id == null) {
            return null;
        }
        BuffType type = BuffRegistry.get(id);
        if (type == null) {
            return null;
        }
        BuffInstance instance = new BuffInstance(type);
        instance.amplifier = Math.max(0, Math.min(tag.getInt("Amplifier"), type.getMaxAmplifier()));
        instance.duration = normalizeDuration(tag.contains("Duration", 3) ? tag.getInt("Duration") : -1);
        instance.elapsedTicks = Math.max(0L, tag.getLong("Elapsed"));
        instance.damageCounter = Math.max(0, tag.getInt("DamageCounter"));
        instance.sourceUuid = tag.hasUUID("Source") ? tag.getUUID("Source") : null;
        instance.sourceName = tag.contains("SourceName", 8) ? tag.getString("SourceName") : null;
        if (tag.contains("Extra", 10)) {
            instance.extra = tag.getCompound("Extra").copy();
        }
        return instance;
    }

    public BuffInstance copy() {
        BuffInstance copy = new BuffInstance(type);
        copy.amplifier = amplifier;
        copy.duration = duration;
        copy.elapsedTicks = elapsedTicks;
        copy.damageCounter = damageCounter;
        copy.sourceUuid = sourceUuid;
        copy.sourceName = sourceName;
        copy.extra = extra.copy();
        return copy;
    }
}
