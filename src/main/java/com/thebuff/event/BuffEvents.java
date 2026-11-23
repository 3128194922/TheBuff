package com.thebuff.event;

import com.thebuff.TheBuff;
import com.thebuff.TheBuffConfig;
import com.thebuff.api.BuffApi;
import com.thebuff.capability.BuffCapability;
import com.thebuff.capability.BuffHolder;
import com.thebuff.capability.BuffProvider;
import com.thebuff.command.TheBuffCommand;
import com.thebuff.network.BuffSync;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TheBuff.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BuffEvents {
    private BuffEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            event.addCapability(new ResourceLocation(TheBuff.MODID, "buffs"), new BuffProvider(living));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        entity.getCapability(BuffCapability.BUFFS).ifPresent(BuffHolder::tick);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        event.getEntity().getCapability(BuffCapability.BUFFS).ifPresent(holder -> {
            holder.reapplyModifiers();
            BuffSync.syncIfPlayer(event.getEntity());
        });
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide || event.isCanceled() || !TheBuffConfig.clearOnDeath) {
            return;
        }
        event.getEntity().getCapability(BuffCapability.BUFFS)
                .ifPresent(holder -> holder.removeAll(com.thebuff.api.BuffRemovalReason.DEATH));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && TheBuffConfig.clearOnDeath) {
            return;
        }
        event.getOriginal().getCapability(BuffCapability.BUFFS).ifPresent(original ->
                event.getEntity().getCapability(BuffCapability.BUFFS).ifPresent(copy -> {
                    copy.copyFrom(original);
                    copy.reapplyModifiers();
                }));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(BuffCapability.BUFFS).ifPresent(holder -> holder.reapplyModifiers());
            BuffSync.syncIfPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(BuffCapability.BUFFS).ifPresent(holder -> holder.reapplyModifiers());
            BuffSync.syncIfPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(BuffCapability.BUFFS).ifPresent(holder -> holder.reapplyModifiers());
            BuffSync.syncIfPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        int interval = Math.max(5, TheBuffConfig.syncIntervalTicks);
        if (player.tickCount % interval != 0) {
            return;
        }
        player.getCapability(BuffCapability.BUFFS).ifPresent(holder -> {
            if (holder.hasTimedBuffs()) {
                BuffSync.syncIfPlayer(player);
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TheBuffCommand.register(event.getDispatcher());
    }
}
