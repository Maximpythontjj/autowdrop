package com.autowaterdrop.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoWaterDropConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("autowaterdrop.json");
    
    // Configuration fields with defaults
    private int minFallHeight = 5;
    private int waterPlacementTicks = 3;
    private boolean autoReturnSlot = true;
    private boolean autoPickupWater = false;
    private boolean searchNearbyBlocks = true;
    private boolean showHudIndicator = true;
    private boolean safeModeExtraTick = false;
    private boolean showNotifications = false;
    private boolean playSounds = false;
    private int pingCompensationTicks = 0;
    
    public static AutoWaterDropConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, AutoWaterDropConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        AutoWaterDropConfig config = new AutoWaterDropConfig();
        config.save();
        return config;
    }
    
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Getters and setters
    public int getMinFallHeight() {
        return minFallHeight;
    }
    
    public void setMinFallHeight(int minFallHeight) {
        this.minFallHeight = Math.max(3, Math.min(20, minFallHeight));
        save();
    }
    
    public int getWaterPlacementTicks() {
        return waterPlacementTicks;
    }
    
    public void setWaterPlacementTicks(int waterPlacementTicks) {
        this.waterPlacementTicks = Math.max(1, Math.min(6, waterPlacementTicks));
        save();
    }
    
    public boolean isAutoReturnSlot() {
        return autoReturnSlot;
    }
    
    public void setAutoReturnSlot(boolean autoReturnSlot) {
        this.autoReturnSlot = autoReturnSlot;
        save();
    }
    
    public boolean isAutoPickupWater() {
        return autoPickupWater;
    }
    
    public void setAutoPickupWater(boolean autoPickupWater) {
        this.autoPickupWater = autoPickupWater;
        save();
    }
    
    public boolean isSearchNearbyBlocks() {
        return searchNearbyBlocks;
    }
    
    public void setSearchNearbyBlocks(boolean searchNearbyBlocks) {
        this.searchNearbyBlocks = searchNearbyBlocks;
        save();
    }
    
    public boolean isShowHudIndicator() {
        return showHudIndicator;
    }
    
    public void setShowHudIndicator(boolean showHudIndicator) {
        this.showHudIndicator = showHudIndicator;
        save();
    }
    
    public boolean isSafeModeExtraTick() {
        return safeModeExtraTick;
    }
    
    public void setSafeModeExtraTick(boolean safeModeExtraTick) {
        this.safeModeExtraTick = safeModeExtraTick;
        save();
    }
    
    public boolean isShowNotifications() {
        return showNotifications;
    }
    
    public void setShowNotifications(boolean showNotifications) {
        this.showNotifications = showNotifications;
        save();
    }
    
    public boolean isPlaySounds() {
        return playSounds;
    }
    
    public void setPlaySounds(boolean playSounds) {
        this.playSounds = playSounds;
        save();
    }
    
    public int getPingCompensationTicks() {
        return pingCompensationTicks;
    }
    
    public void setPingCompensationTicks(int pingCompensationTicks) {
        this.pingCompensationTicks = Math.max(0, Math.min(5, pingCompensationTicks));
        save();
    }
    
    public int getEffectivePlacementTicks() {
        int ticks = waterPlacementTicks + pingCompensationTicks;
        if (safeModeExtraTick) {
            ticks += 1;
        }
        return ticks;
    }
}