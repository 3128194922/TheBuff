package com.thebuff.compat.kubejs;

import com.thebuff.api.BuffCategory;
import com.thebuff.api.BuffRemovalReason;
import com.thebuff.api.BuffType;
import com.thebuff.registry.BuffRegistry;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public final class TheBuffKubeJSPlugin extends KubeJSPlugin {
    public static RegistryInfo<BuffType> info() {
        return RegistryInfo.of(BuffRegistry.KEY, BuffType.class);
    }

    @Override
    public void init() {
        info().addType("basic", BuffBuilder.class, BuffBuilder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("TheBuff", TheBuffBindings.INSTANCE);
        event.add("BuffType", BuffType.class);
        event.add("BuffCategory", BuffCategory.class);
        event.add("BuffRemovalReason", BuffRemovalReason.class);
    }
}
