package com.thebuff.network;

import com.thebuff.api.BuffApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record BuffSyncPacket(int entityId, List<BuffSnapshot> buffs) {
    public static BuffSyncPacket of(ServerPlayer player) {
        List<BuffSnapshot> snapshots = new ArrayList<>();
        for (var instance : BuffApi.getAll(player)) {
            snapshots.add(BuffSnapshot.of(instance));
        }
        return new BuffSyncPacket(player.getId(), snapshots);
    }

    public static void encode(BuffSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.entityId);
        buf.writeVarInt(packet.buffs.size());
        for (BuffSnapshot snapshot : packet.buffs) {
            snapshot.encode(buf);
        }
    }

    public static BuffSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        int size = buf.readVarInt();
        List<BuffSnapshot> buffs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            buffs.add(BuffSnapshot.decode(buf));
        }
        return new BuffSyncPacket(entityId, List.copyOf(buffs));
    }

    public static void handle(BuffSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> com.thebuff.client.ClientBuffCache.accept(packet)
        ));
        context.setPacketHandled(true);
    }
}
