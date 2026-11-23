package com.thebuff.network;

import com.thebuff.TheBuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BuffNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TheBuff.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private BuffNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, BuffSyncPacket.class,
                BuffSyncPacket::encode,
                BuffSyncPacket::decode,
                BuffSyncPacket::handle);
    }

    public static void sendTo(ServerPlayer player, BuffSyncPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
