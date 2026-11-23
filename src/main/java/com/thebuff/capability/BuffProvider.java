package com.thebuff.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BuffProvider implements ICapabilitySerializable<CompoundTag> {
    private final BuffHolder holder;
    private final LazyOptional<BuffHolder> optional;

    public BuffProvider(LivingEntity entity) {
        this.holder = new BuffHolder(entity);
        this.optional = LazyOptional.of(() -> holder);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BuffCapability.BUFFS.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return holder.save();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        holder.load(nbt);
    }
}
