package com.thebuff.api.event;

import com.thebuff.api.BuffInstance;
import com.thebuff.api.BuffRemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the Forge bus after a buff has been removed from a living entity.
 * <p>
 * The event is a read-only notification: the buff is already gone and attribute
 * modifiers have already been detached. The KubeJS bridge forwards it to
 * {@code TheBuffEvents.removed} (all reasons) and {@code TheBuffEvents.expired}
 * (reason {@link BuffRemovalReason#EXPIRED}).
 */
public class BuffRemovedEvent extends Event {
    private final LivingEntity entity;
    private final BuffInstance instance;
    private final BuffRemovalReason reason;

    public BuffRemovedEvent(LivingEntity entity, BuffInstance instance, BuffRemovalReason reason) {
        this.entity = entity;
        this.instance = instance;
        this.reason = reason;
    }

    /** The entity the buff was attached to. */
    public LivingEntity getEntity() {
        return entity;
    }

    /** Snapshot of the removed buff (id, amplifier, remaining duration, source, extra NBT). */
    public BuffInstance getInstance() {
        return instance;
    }

    public BuffRemovalReason getReason() {
        return reason;
    }

    /** True when the buff ran out of duration. */
    public boolean isExpired() {
        return reason == BuffRemovalReason.EXPIRED;
    }

    /** True for explicit API/command removal ({@code REMOVED} or {@code CLEARED}). */
    public boolean isManual() {
        return reason == BuffRemovalReason.REMOVED || reason == BuffRemovalReason.CLEARED;
    }
}
