package com.thebuff.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class BuffSync {
    private BuffSync() {
    }

    public static void syncIfPlayer(Entity entity) {
        if (entity instanceof ServerPlayer player && !player.level().isClientSide) {
            BuffNetwork.sendTo(player, BuffSyncPacket.of(player));
        }
    }
}
