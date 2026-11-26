package com.thebuff.compat.kubejs;

import com.thebuff.api.BuffCategory;
import com.thebuff.api.BuffRemovalReason;
import com.thebuff.api.BuffType;
import com.thebuff.api.event.BuffRemovedEvent;
import com.thebuff.compat.kubejs.event.BuffRemovedEventJS;
import com.thebuff.compat.kubejs.event.TheBuffEvents;
import com.thebuff.registry.BuffRegistry;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

public final class TheBuffKubeJSPlugin extends KubeJSPlugin {
    private static boolean forgeListenerRegistered;

    public TheBuffKubeJSPlugin() {
        registerForgeListener();
    }

    public static RegistryInfo<BuffType> info() {
        return RegistryInfo.of(BuffRegistry.KEY, BuffType.class);
    }

    @Override
    public void init() {
        info().addType("basic", BuffBuilder.class, BuffBuilder::new);
    }

    @Override
    public void registerEvents() {
        TheBuffEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("TheBuff", TheBuffBindings.INSTANCE);
        event.add("BuffType", BuffType.class);
        event.add("BuffCategory", BuffCategory.class);
        event.add("BuffRemovalReason", BuffRemovalReason.class);
    }

    private static synchronized void registerForgeListener() {
        if (forgeListenerRegistered) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(TheBuffKubeJSPlugin::onBuffRemoved);
        forgeListenerRegistered = true;
    }

    private static void onBuffRemoved(BuffRemovedEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        TheBuffEvents.REMOVED.post(ScriptType.SERVER,
                new BuffRemovedEventJS(server, entity, event.getInstance(), event.getReason()));
        if (event.isExpired()) {
            TheBuffEvents.EXPIRED.post(ScriptType.SERVER,
                    new BuffRemovedEventJS(server, entity, event.getInstance(), event.getReason()));
        }
    }
}
