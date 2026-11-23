package com.thebuff.capability;

import com.thebuff.TheBuff;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TheBuff.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BuffCapability {
    public static final Capability<BuffHolder> BUFFS = CapabilityManager.get(new CapabilityToken<>() {
    });

    private BuffCapability() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(BuffHolder.class);
    }
}
