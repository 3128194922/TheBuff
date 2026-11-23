package com.thebuff.registry;

import com.thebuff.TheBuff;
import com.thebuff.api.BuffType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public final class BuffRegistry {
    public static final ResourceLocation KEY_ID = new ResourceLocation(TheBuff.MODID, "buff");
    public static final ResourceKey<Registry<BuffType>> KEY = ResourceKey.createRegistryKey(KEY_ID);
    public static final DeferredRegister<BuffType> BUFFS = DeferredRegister.create(KEY, TheBuff.MODID);

    private BuffRegistry() {
    }

    @Nullable
    public static IForgeRegistry<BuffType> registry() {
        return RegistryManager.ACTIVE.getRegistry(KEY);
    }

    @Nullable
    public static BuffType get(ResourceLocation id) {
        IForgeRegistry<BuffType> registry = registry();
        return registry == null ? null : registry.getValue(id);
    }

    @Nullable
    public static ResourceLocation id(BuffType type) {
        IForgeRegistry<BuffType> registry = registry();
        return registry == null ? null : registry.getKey(type);
    }

    public static Collection<BuffType> all() {
        IForgeRegistry<BuffType> registry = registry();
        return registry == null ? Collections.emptyList() : registry.getValues();
    }

    public static ResourceLocation id(String path) {
        ResourceLocation parsed = ResourceLocation.tryParse(path);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid buff id: " + path);
        }
        if (path.indexOf(':') < 0) {
            return new ResourceLocation(TheBuff.MODID, path);
        }
        return parsed;
    }
}
