package com.autowaterdrop.client;

import com.autowaterdrop.client.config.AutoWaterDropConfig;
import com.autowaterdrop.client.feature.WaterDropManager;
import com.autowaterdrop.client.hud.HudRenderer;
import com.autowaterdrop.client.util.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoWaterDropClient implements ClientModInitializer {
    public static final String MOD_ID = "autowaterdrop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static AutoWaterDropClient instance;
    private AutoWaterDropConfig config;
    private WaterDropManager waterDropManager;
    private HudRenderer hudRenderer;
    private boolean enabled = true;
    
    @Override
    public void onInitializeClient() {
        instance = this;
        
        // Initialize configuration
        config = AutoWaterDropConfig.load();
        
        // Initialize components
        waterDropManager = new WaterDropManager(this);
        hudRenderer = new HudRenderer(this);
        
        // Register key bindings
        KeyBindings.register();
        
        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        // Register HUD render event
        HudRenderCallback.EVENT.register(hudRenderer::render);
        
        LOGGER.info("Auto WaterDrop initialized successfully!");
    }
    
    private void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        
        // Handle key bindings
        KeyBindings.handleKeys(client);
        
        // Process water drop logic
        if (enabled) {
            waterDropManager.tick(client);
        }
    }
    
    public static AutoWaterDropClient getInstance() {
        return instance;
    }
    
    public AutoWaterDropConfig getConfig() {
        return config;
    }
    
    public WaterDropManager getWaterDropManager() {
        return waterDropManager;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }
}