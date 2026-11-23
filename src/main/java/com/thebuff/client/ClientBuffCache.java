package com.thebuff.client;

import com.thebuff.network.BuffSnapshot;
import com.thebuff.network.BuffSyncPacket;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientBuffCache {
    private static final Map<Integer, List<ClientBuff>> CACHE = new HashMap<>();

    private ClientBuffCache() {
    }

    public static void accept(BuffSyncPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.player.getId() != packet.entityId()) {
            return;
        }
        long now = minecraft.level.getGameTime();
        List<ClientBuff> buffs = packet.buffs().stream()
                .map(snapshot -> toClientBuff(snapshot, now))
                .toList();
        CACHE.put(packet.entityId(), buffs);
    }

    private static ClientBuff toClientBuff(BuffSnapshot snapshot, long now) {
        long expireAt = snapshot.permanent() ? -1L : now + snapshot.duration();
        return new ClientBuff(
                snapshot.id(),
                snapshot.name(),
                snapshot.color(),
                snapshot.category(),
                snapshot.amplifier(),
                expireAt,
                snapshot.permanent(),
                snapshot.showInHud()
        );
    }

    public static List<ClientBuff> get(int entityId) {
        return CACHE.getOrDefault(entityId, List.of());
    }

    public static void clear() {
        CACHE.clear();
    }
}
