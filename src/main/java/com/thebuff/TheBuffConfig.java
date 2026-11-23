package com.thebuff;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = TheBuff.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TheBuffConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    private static final ForgeConfigSpec.BooleanValue CLEAR_ON_DEATH;
    private static final ForgeConfigSpec.IntValue MAX_BUFFS_PER_ENTITY;
    private static final ForgeConfigSpec.IntValue SYNC_INTERVAL_TICKS;

    private static final ForgeConfigSpec.BooleanValue HUD_ENABLED;
    private static final ForgeConfigSpec.IntValue HUD_OFFSET_X;
    private static final ForgeConfigSpec.IntValue HUD_OFFSET_Y;
    private static final ForgeConfigSpec.BooleanValue HUD_SHOW_LEVEL;

    public static boolean clearOnDeath = true;
    public static int maxBuffsPerEntity = 32;
    public static int syncIntervalTicks = 20;

    public static boolean hudEnabled = true;
    public static int hudOffsetX = 0;
    public static int hudOffsetY = 0;
    public static boolean hudShowLevel = true;

    static {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        common.comment("General behaviour of The Buff.").push("common");
        CLEAR_ON_DEATH = common.comment("Clear special buffs when a living entity dies.")
                .define("clearOnDeath", true);
        MAX_BUFFS_PER_ENTITY = common.comment("Maximum number of different buffs on one entity.")
                .defineInRange("maxBuffsPerEntity", 32, 1, 1024);
        SYNC_INTERVAL_TICKS = common.comment("How often timed buffs are re-synced to the owning player.")
                .defineInRange("syncIntervalTicks", 20, 5, 200);
        common.pop();
        COMMON_SPEC = common.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.comment("Client-side HUD settings.").push("hud");
        HUD_ENABLED = client.define("enabled", true);
        HUD_OFFSET_X = client.comment("Negative values move the text left.")
                .defineInRange("offsetX", 0, -1000, 1000);
        HUD_OFFSET_Y = client.comment("Positive values move the text down.")
                .defineInRange("offsetY", 0, -1000, 1000);
        HUD_SHOW_LEVEL = client.define("showLevel", true);
        client.pop();
        CLIENT_SPEC = client.build();
    }

    private TheBuffConfig() {
    }

    @SubscribeEvent
    public static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            clearOnDeath = CLEAR_ON_DEATH.get();
            maxBuffsPerEntity = MAX_BUFFS_PER_ENTITY.get();
            syncIntervalTicks = SYNC_INTERVAL_TICKS.get();
        } else if (event.getConfig().getSpec() == CLIENT_SPEC) {
            hudEnabled = HUD_ENABLED.get();
            hudOffsetX = HUD_OFFSET_X.get();
            hudOffsetY = HUD_OFFSET_Y.get();
            hudShowLevel = HUD_SHOW_LEVEL.get();
        }
    }
}
