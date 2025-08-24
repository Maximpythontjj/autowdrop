package com.autowaterdrop.client.hud;

import com.autowaterdrop.client.AutoWaterDropClient;
import com.autowaterdrop.client.feature.WaterDropManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class HudRenderer {
    private final AutoWaterDropClient modClient;
    private static final ItemStack WATER_BUCKET = new ItemStack(Items.WATER_BUCKET);
    private static final ItemStack BUCKET = new ItemStack(Items.BUCKET);
    
    public HudRenderer(AutoWaterDropClient modClient) {
        this.modClient = modClient;
    }
    
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (!modClient.getConfig().isShowHudIndicator()) {
            return;
        }
        
        if (client.player == null || client.options.hudHidden) {
            return;
        }
        
        if (!modClient.isEnabled()) {
            return;
        }
        
        WaterDropManager manager = modClient.getWaterDropManager();
        boolean isReady = manager.isReady();
        boolean isFalling = manager.isFalling();
        
        // Position in top-left corner
        int x = 5;
        int y = 5;
        
        // Draw background
        int bgColor = isReady ? 0x4000FF00 : 0x40FF0000; // Green if ready, red if not
        if (isFalling) {
            bgColor = 0x40FFFF00; // Yellow when falling
        }
        
        context.fill(x - 2, y - 2, x + 20, y + 20, bgColor);
        context.fill(x - 1, y - 1, x + 19, y + 19, 0xFF000000);
        
        // Draw bucket icon
        ItemStack displayItem = isReady ? WATER_BUCKET : BUCKET;
        context.drawItem(displayItem, x, y);
        
        // Draw status text
        String statusText;
        int textColor;
        
        if (isFalling) {
            statusText = "FALLING";
            textColor = 0xFFFF00;
        } else if (isReady) {
            statusText = "READY";
            textColor = 0x00FF00;
        } else {
            statusText = "NO WATER";
            textColor = 0xFF0000;
        }
        
        context.drawTextWithShadow(
            client.textRenderer,
            statusText,
            x + 22,
            y + 5,
            textColor
        );
        
        // Draw mod name
        context.drawTextWithShadow(
            client.textRenderer,
            "Auto WaterDrop",
            x,
            y + 22,
            0xFFFFFF
        );
    }
}