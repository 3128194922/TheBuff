package com.thebuff;

import com.mojang.logging.LogUtils;
import com.thebuff.api.BuffType;
import com.thebuff.api.BuffTypes;
import com.thebuff.network.BuffNetwork;
import com.thebuff.registry.BuffRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryBuilder;
import org.slf4j.Logger;

@Mod(TheBuff.MODID)
public final class TheBuff {
    public static final String MODID = "thebuff";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheBuff(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        BuffRegistry.BUFFS.makeRegistry(() -> new RegistryBuilder<BuffType>()
                .disableSaving()
                .disableSync());
        BuffRegistry.BUFFS.register(modBus);
        BuffTypes.init();

        modBus.addListener(this::commonSetup);
        context.registerConfig(ModConfig.Type.COMMON, TheBuffConfig.COMMON_SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.registerConfig(ModConfig.Type.CLIENT, TheBuffConfig.CLIENT_SPEC);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BuffNetwork::register);
    }
}
