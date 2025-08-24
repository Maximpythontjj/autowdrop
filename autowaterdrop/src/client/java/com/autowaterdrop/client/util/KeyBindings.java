package com.autowaterdrop.client.util;

import com.autowaterdrop.client.AutoWaterDropClient;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyBinding toggleKey;
    private static KeyBinding configKey;
    
    public static void register() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autowaterdrop.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.autowaterdrop"
        ));
        
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autowaterdrop.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.autowaterdrop"
        ));
    }
    
    public static void handleKeys(MinecraftClient client) {
        if (toggleKey.wasPressed()) {
            AutoWaterDropClient.getInstance().toggleEnabled();
            boolean enabled = AutoWaterDropClient.getInstance().isEnabled();
            
            if (client.player != null) {
                String status = enabled ? "§aEnabled" : "§cDisabled";
                client.player.sendMessage(
                    Text.literal("§b[Auto WaterDrop] " + status), 
                    true
                );
            }
        }
        
        if (configKey.wasPressed()) {
            // Open config screen (will be implemented with ModMenu integration)
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§b[Auto WaterDrop] §eConfig screen requires ModMenu"), 
                    true
                );
            }
        }
    }
}