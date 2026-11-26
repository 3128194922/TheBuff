package com.thebuff.compat.kubejs.event;

import com.thebuff.api.BuffDamage;
import com.thebuff.api.BuffInstance;
import com.thebuff.api.BuffRemovalReason;
import com.thebuff.api.BuffType;
import dev.latvian.mods.kubejs.server.ServerEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Payload for {@code TheBuffEvents.removed} and {@code TheBuffEvents.expired}.
 */
public class BuffRemovedEventJS extends ServerEventJS {
    private final LivingEntity entity;
    private final BuffInstance instance;
    private final BuffRemovalReason reason;

    public BuffRemovedEventJS(MinecraftServer server, LivingEntity entity,
                              BuffInstance instance, BuffRemovalReason reason) {
        super(server);
        this.entity = entity;
        this.instance = instance;
        this.reason = reason;
    }

    /** The entity the buff was attached to. */
    public LivingEntity getEntity() {
        return entity;
    }

    /** The entity as a player, or null for non-player living entities. */
    @Nullable
    public Player getPlayer() {
        return entity instanceof Player player ? player : null;
    }

    /** Snapshot of the removed buff. */
    public BuffInstance getInstance() {
        return instance;
    }

    public BuffInstance getBuff() {
        return instance;
    }

    public BuffType getType() {
        return instance.getType();
    }

    public ResourceLocation getId() {
        return instance.getId();
    }

    /** Buff id as a string, e.g. {@code "thebuff:burn"}. */
    public String getBuffId() {
        return instance.getId().toString();
    }

    public int getAmplifier() {
        return instance.getAmplifier();
    }

    /** Remaining duration at the moment of removal; {@code -1} for permanent buffs. */
    public int getDuration() {
        return instance.getDuration();
    }

    public boolean isPermanent() {
        return instance.isPermanent();
    }

    public long getElapsedTicks() {
        return instance.getElapsedTicks();
    }

    public BuffRemovalReason getReason() {
        return reason;
    }

    /** Lowercase reason name, e.g. {@code "expired"}, for easy string comparison in JS. */
    public String getReasonName() {
        return reason.name().toLowerCase(Locale.ROOT);
    }

    /** True when the buff ran out of duration. */
    public boolean isExpired() {
        return reason == BuffRemovalReason.EXPIRED;
    }

    /** True for explicit API/command removal ({@code REMOVED} or {@code CLEARED}). */
    public boolean isManual() {
        return reason == BuffRemovalReason.REMOVED || reason == BuffRemovalReason.CLEARED;
    }

    /** The entity that applied the buff, if it is still loaded. */
    @Nullable
    public Entity getSource() {
        if (entity.level() instanceof ServerLevel level) {
            return BuffDamage.resolveSource(level, instance.getSourceUuid());
        }
        return null;
    }

    @Nullable
    public String getSourceName() {
        return instance.getSourceName();
    }
}
