package com.thebuff.client;

import com.thebuff.TheBuff;
import com.thebuff.TheBuffConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = TheBuff.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BuffHudRenderer {
    private BuffHudRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui || !TheBuffConfig.hudEnabled) {
            return;
        }

        List<ClientBuff> buffs = ClientBuffCache.get(minecraft.player.getId());
        if (buffs.isEmpty()) {
            return;
        }

        int visibleEffects = 0;
        for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
            if (effect.showIcon()) {
                visibleEffects++;
            }
        }

        int right = minecraft.getWindow().getGuiScaledWidth() - 4 + TheBuffConfig.hudOffsetX;
        int y = 1 + visibleEffects * 26 + TheBuffConfig.hudOffsetY;
        long gameTime = minecraft.level.getGameTime();

        for (ClientBuff buff : buffs) {
            if (!buff.showInHud()) {
                continue;
            }

            int remaining = buff.remaining(gameTime);
            if (!buff.permanent() && remaining <= 0) {
                continue;
            }

            Component duration = buff.permanent()
                    ? Component.translatable("thebuff.duration.permanent")
                    : Component.literal(StringUtil.formatTickDuration(remaining));
            Component line;
            if (TheBuffConfig.hudShowLevel) {
                Component level = Component.translatable("thebuff.hud.level", buff.amplifier() + 1);
                line = Component.translatable("thebuff.hud.line", buff.name(), level, duration);
            } else {
                line = Component.translatable("thebuff.hud.line.no_level", buff.name(), duration);
            }

            int x = right - minecraft.font.width(line);
            event.getGuiGraphics().drawString(
                    minecraft.font,
                    line,
                    x,
                    y,
                    0xFF000000 | (buff.color() & 0xFFFFFF),
                    true
            );
            y += 10;
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBuffCache.clear();
    }
}
