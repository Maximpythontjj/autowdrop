package com.autowaterdrop.client.config;

import com.autowaterdrop.client.AutoWaterDropClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class ModMenuIntegration {
    public static Screen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }
    
    private static class ConfigScreen extends Screen {
        private final Screen parent;
        private final AutoWaterDropConfig config;
        
        protected ConfigScreen(Screen parent) {
            super(Text.literal("Auto WaterDrop Configuration"));
            this.parent = parent;
            this.config = AutoWaterDropClient.getInstance().getConfig();
        }
        
        @Override
        protected void init() {
            int centerX = this.width / 2;
            int startY = 40;
            int spacing = 25;
            int widgetWidth = 200;
            int widgetHeight = 20;
            
            // Title
            this.addDrawableChild(new TextWidget(
                centerX - 100, 20, 200, 20,
                Text.literal("Auto WaterDrop Settings"),
                this.textRenderer
            ));
            
            // Min Fall Height Slider
            this.addDrawableChild(new SliderWidget(
                centerX - widgetWidth / 2, startY,
                widgetWidth, widgetHeight,
                Text.literal("Min Fall Height: " + config.getMinFallHeight()),
                (config.getMinFallHeight() - 3) / 17.0
            ) {
                @Override
                protected void updateMessage() {
                    int value = (int) (3 + this.value * 17);
                    this.setMessage(Text.literal("Min Fall Height: " + value));
                }
                
                @Override
                protected void applyValue() {
                    int value = (int) (3 + this.value * 17);
                    config.setMinFallHeight(value);
                }
            });
            
            // Water Placement Ticks Slider
            this.addDrawableChild(new SliderWidget(
                centerX - widgetWidth / 2, startY + spacing,
                widgetWidth, widgetHeight,
                Text.literal("Placement Timing: " + config.getWaterPlacementTicks() + " ticks"),
                (config.getWaterPlacementTicks() - 1) / 5.0
            ) {
                @Override
                protected void updateMessage() {
                    int value = (int) (1 + this.value * 5);
                    this.setMessage(Text.literal("Placement Timing: " + value + " ticks"));
                }
                
                @Override
                protected void applyValue() {
                    int value = (int) (1 + this.value * 5);
                    config.setWaterPlacementTicks(value);
                }
            });
            
            // Auto Return Slot Toggle
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto Return Slot: " + (config.isAutoReturnSlot() ? "ON" : "OFF")),
                button -> {
                    config.setAutoReturnSlot(!config.isAutoReturnSlot());
                    button.setMessage(Text.literal("Auto Return Slot: " + (config.isAutoReturnSlot() ? "ON" : "OFF")));
                }
            ).dimensions(centerX - widgetWidth / 2, startY + spacing * 2, widgetWidth, widgetHeight).build());
            
            // Auto Pickup Water Toggle
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto Pickup Water: " + (config.isAutoPickupWater() ? "ON" : "OFF")),
                button -> {
                    config.setAutoPickupWater(!config.isAutoPickupWater());
                    button.setMessage(Text.literal("Auto Pickup Water: " + (config.isAutoPickupWater() ? "ON" : "OFF")));
                }
            ).dimensions(centerX - widgetWidth / 2, startY + spacing * 3, widgetWidth, widgetHeight).build());
            
            // Search Nearby Blocks Toggle
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Search Nearby: " + (config.isSearchNearbyBlocks() ? "ON" : "OFF")),
                button -> {
                    config.setSearchNearbyBlocks(!config.isSearchNearbyBlocks());
                    button.setMessage(Text.literal("Search Nearby: " + (config.isSearchNearbyBlocks() ? "ON" : "OFF")));
                }
            ).dimensions(centerX - widgetWidth / 2, startY + spacing * 4, widgetWidth, widgetHeight).build());
            
            // Show HUD Indicator Toggle
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("HUD Indicator: " + (config.isShowHudIndicator() ? "ON" : "OFF")),
                button -> {
                    config.setShowHudIndicator(!config.isShowHudIndicator());
                    button.setMessage(Text.literal("HUD Indicator: " + (config.isShowHudIndicator() ? "ON" : "OFF")));
                }
            ).dimensions(centerX - widgetWidth / 2, startY + spacing * 5, widgetWidth, widgetHeight).build());
            
            // Safe Mode Toggle
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Safe Mode: " + (config.isSafeModeExtraTick() ? "ON" : "OFF")),
                button -> {
                    config.setSafeModeExtraTick(!config.isSafeModeExtraTick());
                    button.setMessage(Text.literal("Safe Mode: " + (config.isSafeModeExtraTick() ? "ON" : "OFF")));
                }
            ).dimensions(centerX - widgetWidth / 2, startY + spacing * 6, widgetWidth, widgetHeight).build());
            
            // Show Notifications Toggle
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Notifications: " + (config.isShowNotifications() ? "ON" : "OFF")),
                button -> {
                    config.setShowNotifications(!config.isShowNotifications());
                    button.setMessage(Text.literal("Notifications: " + (config.isShowNotifications() ? "ON" : "OFF")));
                }
            ).dimensions(centerX - widgetWidth / 2, startY + spacing * 7, widgetWidth, widgetHeight).build());
            
            // Done button
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.close()
            ).dimensions(centerX - 100, this.height - 30, 200, 20).build());
        }
        
        @Override
        public void close() {
            config.save();
            this.client.setScreen(parent);
        }
    }
}