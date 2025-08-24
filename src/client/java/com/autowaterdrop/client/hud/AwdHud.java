package com.autowaterdrop.client.hud;

import com.autowaterdrop.client.config.AwdConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class AwdHud {
    private static boolean ready;

    public static void setReady(boolean isReady) {
        ready = isReady;
    }

    public static void render(DrawContext draw) {
        if (!AwdConfig.get().showHud) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) return;
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        int size = 12;
        int x = width - size - 6;
        int y = height - size - 28;

        int color = ready ? 0x8800FF00 : 0x88FF0000;
        draw.fill(x, y, x + size, y + size, color);
    }
}

