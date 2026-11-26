package com.thebuff.capability;

import com.thebuff.TheBuff;
import com.thebuff.TheBuffConfig;
import com.thebuff.api.AttributeModifierSpec;
import com.thebuff.api.BuffCategory;
import com.thebuff.api.BuffContext;
import com.thebuff.api.BuffDamage;
import com.thebuff.api.BuffInstance;
import com.thebuff.api.BuffRemovalReason;
import com.thebuff.api.BuffTickHandler;
import com.thebuff.api.BuffType;
import com.thebuff.api.DamageSpec;
import com.thebuff.api.event.BuffRemovedEvent;
import com.thebuff.network.BuffSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BuffHolder {
    private final LivingEntity entity;
    private final Map<ResourceLocation, BuffInstance> buffs = new LinkedHashMap<>();

    public BuffHolder(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public List<BuffInstance> getBuffs() {
        return List.copyOf(buffs.values());
    }

    @Nullable
    public BuffInstance get(ResourceLocation id) {
        return buffs.get(id);
    }

    public boolean has(ResourceLocation id) {
        return buffs.containsKey(id);
    }

    public boolean hasTimedBuffs() {
        for (BuffInstance instance : buffs.values()) {
            if (!instance.isPermanent()) {
                return true;
            }
        }
        return false;
    }

    public boolean apply(BuffInstance incoming, @Nullable Entity source) {
        if (incoming == null) {
            return false;
        }
        if (source != null) {
            incoming.setSource(source.getUUID(), source.getDisplayName().getString());
        }

        ResourceLocation id = incoming.getId();
        BuffInstance existing = buffs.get(id);
        if (existing == null) {
            if (buffs.size() >= TheBuffConfig.maxBuffsPerEntity) {
                TheBuff.LOGGER.warn("Refusing to add {} to {}: buff limit {} reached",
                        id, entity.getScoreboardName(), TheBuffConfig.maxBuffsPerEntity);
                return false;
            }

            buffs.put(id, incoming);
            applyModifiers(incoming);
            callOnApply(incoming, true);

            DamageSpec spec = incoming.getType().getDamage();
            if (spec != null && spec.enabled() && spec.applyImmediately()) {
                damage(incoming, damageAmount(incoming, spec), null);
            }

            markChanged();
            return true;
        }

        boolean changed = false;
        boolean levelChanged = false;

        if (incoming.getAmplifier() > existing.getAmplifier()) {
            existing.setAmplifier(incoming.getAmplifier());
            levelChanged = true;
            changed = true;
        }

        int mergedDuration = mergeDuration(existing.getDuration(), incoming.getDuration());
        if (mergedDuration != existing.getDuration()) {
            existing.setDuration(mergedDuration);
            changed = true;
        }

        if (incoming.getSourceUuid() != null) {
            existing.setSource(incoming.getSourceUuid(), incoming.getSourceName());
            changed = true;
        }

        if (!incoming.getExtra().isEmpty()) {
            existing.setExtra(incoming.getExtra());
            changed = true;
        }

        if (levelChanged) {
            removeModifiers(existing);
            applyModifiers(existing);
        }

        callOnApply(existing, false);

        DamageSpec spec = existing.getType().getDamage();
        if (levelChanged && spec != null && spec.enabled() && spec.applyImmediately()) {
            damage(existing, damageAmount(existing, spec), null);
        }

        if (changed) {
            markChanged();
        }
        return true;
    }

    public boolean remove(ResourceLocation id, BuffRemovalReason reason) {
        if (removeInternal(id, reason) == null) {
            return false;
        }
        markChanged();
        return true;
    }

    public int removeAll(BuffRemovalReason reason) {
        int removed = 0;
        for (ResourceLocation id : new ArrayList<>(buffs.keySet())) {
            if (removeInternal(id, reason) != null) {
                removed++;
            }
        }
        if (removed > 0) {
            markChanged();
        }
        return removed;
    }

    public int removeBySource(@Nullable UUID sourceUuid, BuffRemovalReason reason) {
        if (sourceUuid == null) {
            return 0;
        }
        int removed = 0;
        for (ResourceLocation id : new ArrayList<>(buffs.keySet())) {
            BuffInstance instance = buffs.get(id);
            if (instance != null && sourceUuid.equals(instance.getSourceUuid())) {
                if (removeInternal(id, reason) != null) {
                    removed++;
                }
            }
        }
        if (removed > 0) {
            markChanged();
        }
        return removed;
    }

    public int removeCategory(BuffCategory category, BuffRemovalReason reason) {
        int removed = 0;
        for (ResourceLocation id : new ArrayList<>(buffs.keySet())) {
            BuffInstance instance = buffs.get(id);
            if (instance != null && instance.getType().getCategory() == category) {
                if (removeInternal(id, reason) != null) {
                    removed++;
                }
            }
        }
        if (removed > 0) {
            markChanged();
        }
        return removed;
    }

    public boolean setDuration(ResourceLocation id, int duration) {
        BuffInstance instance = buffs.get(id);
        if (instance == null) {
            return false;
        }
        instance.setDuration(duration);
        markChanged();
        return true;
    }

    public boolean refresh(ResourceLocation id, int duration) {
        return setDuration(id, duration);
    }

    public boolean setAmplifier(ResourceLocation id, int amplifier) {
        BuffInstance instance = buffs.get(id);
        if (instance == null) {
            return false;
        }
        int clamped = Math.max(0, Math.min(amplifier, instance.getType().getMaxAmplifier()));
        if (instance.getAmplifier() == clamped) {
            return true;
        }
        removeModifiers(instance);
        instance.setAmplifier(clamped);
        applyModifiers(instance);
        markChanged();
        return true;
    }

    public void tick() {
        if (buffs.isEmpty()) {
            return;
        }

        boolean changed = false;
        for (BuffInstance instance : new ArrayList<>(buffs.values())) {
            if (buffs.get(instance.getId()) != instance) {
                continue;
            }

            // Permanent states with no per-tick work never enter the timer path.
            if (instance.isPermanent() && !instance.getType().needsTick()) {
                continue;
            }

            instance.tickElapsed();

            DamageSpec spec = instance.getType().getDamage();
            if (spec != null && spec.enabled()) {
                int next = instance.getDamageCounter() + 1;
                if (next >= spec.intervalTicks()) {
                    instance.setDamageCounter(0);
                    damage(instance, damageAmount(instance, spec), null);
                } else {
                    instance.setDamageCounter(next);
                }
            }

            if (buffs.get(instance.getId()) != instance) {
                continue;
            }

            BuffTickHandler onTick = instance.getType().getOnTick();
            if (onTick != null) {
                try {
                    onTick.handle(new BuffContext(this, instance, null, false));
                } catch (Throwable throwable) {
                    TheBuff.LOGGER.error("Buff tick handler failed for {}", instance.getId(), throwable);
                }
            }

            if (buffs.get(instance.getId()) != instance) {
                continue;
            }

            if (!instance.isPermanent()) {
                int nextDuration = instance.getDuration() - 1;
                if (nextDuration <= 0) {
                    removeInternal(instance.getId(), BuffRemovalReason.EXPIRED);
                    changed = true;
                } else {
                    instance.setDuration(nextDuration);
                }
            }

            if (!entity.isAlive()) {
                break;
            }
        }

        if (changed) {
            markChanged();
        }
    }

    public boolean damage(BuffInstance instance, double amount, @Nullable ResourceLocation overrideDamageType) {
        if (instance == null || amount <= 0.0D) {
            return false;
        }
        if (buffs.get(instance.getId()) != instance) {
            return false;
        }
        return BuffDamage.deal(entity, instance, amount, overrideDamageType);
    }

    private double damageAmount(BuffInstance instance, DamageSpec spec) {
        return spec.amountPerLevel() * (instance.getAmplifier() + 1);
    }

    @Nullable
    public Entity resolveSource(BuffInstance instance) {
        return BuffDamage.resolveSource(entity.level() instanceof net.minecraft.server.level.ServerLevel level ? level : null,
                instance.getSourceUuid());
    }

    public void reapplyModifiers() {
        if (buffs.isEmpty()) {
            return;
        }
        for (BuffInstance instance : buffs.values()) {
            removeModifiers(instance);
            applyModifiers(instance);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (BuffInstance instance : buffs.values()) {
            list.add(instance.save());
        }
        tag.put("Buffs", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        buffs.clear();
        ListTag list = tag.getList("Buffs", 10);
        for (int i = 0; i < list.size(); i++) {
            BuffInstance instance = BuffInstance.load(list.getCompound(i));
            if (instance == null) {
                TheBuff.LOGGER.warn("Dropping unknown buff entry on {}", entity.getScoreboardName());
                continue;
            }
            buffs.put(instance.getId(), instance);
        }
    }

    public void copyFrom(BuffHolder other) {
        buffs.clear();
        for (BuffInstance instance : other.buffs.values()) {
            buffs.put(instance.getId(), instance.copy());
        }
    }

    @Nullable
    private BuffInstance removeInternal(ResourceLocation id, BuffRemovalReason reason) {
        BuffInstance removed = buffs.remove(id);
        if (removed == null) {
            return null;
        }
        removeModifiers(removed);
        callOnExpire(removed, reason);
        MinecraftForge.EVENT_BUS.post(new BuffRemovedEvent(entity, removed, reason));
        return removed;
    }

    private void callOnApply(BuffInstance instance, boolean newlyApplied) {
        BuffTickHandler handler = instance.getType().getOnApply();
        if (handler == null) {
            return;
        }
        try {
            handler.handle(new BuffContext(this, instance, null, newlyApplied));
        } catch (Throwable throwable) {
            TheBuff.LOGGER.error("Buff apply handler failed for {}", instance.getId(), throwable);
        }
    }

    private void callOnExpire(BuffInstance instance, BuffRemovalReason reason) {
        BuffTickHandler handler = instance.getType().getOnExpire();
        if (handler == null) {
            return;
        }
        try {
            handler.handle(new BuffContext(this, instance, reason, false));
        } catch (Throwable throwable) {
            TheBuff.LOGGER.error("Buff expire handler failed for {}", instance.getId(), throwable);
        }
    }

    private void applyModifiers(BuffInstance instance) {
        List<AttributeModifierSpec> specs = instance.getType().getAttributeModifiers();
        for (int i = 0; i < specs.size(); i++) {
            AttributeModifierSpec spec = specs.get(i);
            AttributeInstance attribute = entity.getAttribute(spec.attribute());
            if (attribute == null) {
                continue;
            }
            UUID modifierId = modifierId(instance, i);
            attribute.removeModifier(modifierId);
            double amount = spec.amountPerLevel() * (instance.getAmplifier() + 1);
            attribute.addTransientModifier(new AttributeModifier(
                    modifierId,
                    "thebuff:" + instance.getId().getPath() + "_" + i,
                    amount,
                    spec.operation()
            ));
        }
    }

    private void removeModifiers(BuffInstance instance) {
        List<AttributeModifierSpec> specs = instance.getType().getAttributeModifiers();
        for (int i = 0; i < specs.size(); i++) {
            AttributeModifierSpec spec = specs.get(i);
            AttributeInstance attribute = entity.getAttribute(spec.attribute());
            if (attribute != null) {
                attribute.removeModifier(modifierId(instance, i));
            }
        }
    }

    private UUID modifierId(BuffInstance instance, int index) {
        String value = entity.getUUID() + "|" + instance.getId() + "|" + index;
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private void markChanged() {
        BuffSync.syncIfPlayer(entity);
    }

    private static int mergeDuration(int current, int incoming) {
        if (current < 0 || incoming < 0) {
            return -1;
        }
        return Math.max(current, incoming);
    }
}
