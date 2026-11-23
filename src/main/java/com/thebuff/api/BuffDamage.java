package com.thebuff.api;

import com.thebuff.TheBuff;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BuffDamage {
    private static final Set<ResourceLocation> WARNED_TYPES = ConcurrentHashMap.newKeySet();

    private BuffDamage() {
    }

    public static boolean deal(
            LivingEntity target,
            BuffInstance instance,
            double amount,
            @Nullable ResourceLocation overrideDamageType
    ) {
        if (amount <= 0.0D || !(target.level() instanceof ServerLevel level)) {
            return false;
        }

        ResourceLocation typeId = overrideDamageType != null
                ? overrideDamageType
                : (instance.getType().getDamage() != null
                ? instance.getType().getDamage().damageType()
                : DamageSpec.DEFAULT_DAMAGE_TYPE);
        Holder<DamageType> type = resolveDamageType(level, typeId);
        if (type == null) {
            return false;
        }

        Entity source = resolveSource(level, instance.getSourceUuid());
        DamageSource damageSource = new BuffDamageSource(type, source, source, instance.getSourceName());

        DamageSpec spec = instance.getType().getDamage();
        if (spec != null && spec.ignoreInvulnerability()) {
            target.invulnerableTime = 0;
        }

        return target.hurt(damageSource, (float) amount);
    }

    @Nullable
    public static Holder<DamageType> resolveDamageType(ServerLevel level, ResourceLocation id) {
        var registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, id);
        if (registry.containsKey(key)) {
            return registry.getHolderOrThrow(key);
        }
        if (!id.equals(DamageSpec.DEFAULT_DAMAGE_TYPE)) {
            ResourceKey<DamageType> fallback = ResourceKey.create(Registries.DAMAGE_TYPE, DamageSpec.DEFAULT_DAMAGE_TYPE);
            if (registry.containsKey(fallback)) {
                if (WARNED_TYPES.add(id)) {
                    TheBuff.LOGGER.warn("Unknown buff damage type {}, using {}", id, DamageSpec.DEFAULT_DAMAGE_TYPE);
                }
                return registry.getHolderOrThrow(fallback);
            }
        }
        if (WARNED_TYPES.add(id)) {
            TheBuff.LOGGER.error("Missing buff damage type {} and fallback {}", id, DamageSpec.DEFAULT_DAMAGE_TYPE);
        }
        return null;
    }

    @Nullable
    public static Entity resolveSource(@Nullable ServerLevel level, @Nullable UUID sourceUuid) {
        if (level == null || sourceUuid == null) {
            return null;
        }

        Entity entity = level.getEntity(sourceUuid);
        if (entity != null) {
            return entity;
        }

        MinecraftServer server = level.getServer();
        ServerPlayer player = server.getPlayerList().getPlayer(sourceUuid);
        if (player != null) {
            return player;
        }

        for (ServerLevel other : server.getAllLevels()) {
            entity = other.getEntity(sourceUuid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
}
