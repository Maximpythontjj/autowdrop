package com.autowaterdrop.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public final class LatencyUtil {
    private LatencyUtil() {}

    public static int getLatencyTicks(MinecraftClient client) {
        if (client == null || client.getNetworkHandler() == null) return 0;
        ClientPlayNetworkHandler nh = client.getNetworkHandler();
        if (client.player != null && nh.getPlayerListEntry(client.player.getUuid()) != null) {
            int ping = nh.getPlayerListEntry(client.player.getUuid()).getLatency(); // ms
            // Convert ms to ticks (50ms per tick), cap to 2 for safety
            return Math.min(2, Math.max(0, (int)Math.round(ping / 50.0)));
        }
        return 0;
    }
}

