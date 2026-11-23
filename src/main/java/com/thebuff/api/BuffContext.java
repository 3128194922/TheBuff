package com.thebuff.api;

import com.thebuff.capability.BuffHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class BuffContext {
    private final BuffHolder holder;
    private final BuffInstance instance;
    @Nullable
    private final BuffRemovalReason reason;
    private final boolean newlyApplied;

    public BuffContext(BuffHolder holder, BuffInstance instance, @Nullable BuffRemovalReason reason, boolean newlyApplied) {
        this.holder = holder;
        this.instance = instance;
        this.reason = reason;
        this.newlyApplied = newlyApplied;
    }

    public LivingEntity getEntity() {
        return holder.getEntity();
    }

    public BuffType getType() {
        return instance.getType();
    }

    public BuffInstance getInstance() {
        return instance;
    }

    public int getAmplifier() {
        return instance.getAmplifier();
    }

    public int getRemainingTicks() {
        return instance.getDuration();
    }

    public int getRemaining() {
        return instance.getDuration();
    }

    public boolean isPermanent() {
        return instance.isPermanent();
    }

    public long getElapsedTicks() {
        return instance.getElapsedTicks();
    }

    public boolean isNewlyApplied() {
        return newlyApplied;
    }

    @Nullable
    public BuffRemovalReason getReason() {
        return reason;
    }

    @Nullable
    public Entity getSource() {
        return holder.resolveSource(instance);
    }

    @Nullable
    public String getSourceName() {
        return instance.getSourceName();
    }

    public CompoundTag getExtra() {
        return instance.getExtra();
    }

    public ServerLevel getServerLevel() {
        return (ServerLevel) getEntity().level();
    }

    public boolean damage(double amount) {
        return holder.damage(instance, amount, null);
    }
}
