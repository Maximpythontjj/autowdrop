package com.autowaterdrop.client.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class InventoryUtil {
    
    /**
     * Find water bucket in hotbar (slots 0-8)
     * @return slot index or -1 if not found
     */
    public static int findWaterBucket(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Find water bucket closest to current selected slot
     * @return slot index or -1 if not found
     */
    public static int findClosestWaterBucket(ClientPlayerEntity player) {
        int currentSlot = player.getInventory().selectedSlot;
        int closestSlot = -1;
        int minDistance = Integer.MAX_VALUE;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                int distance = getSlotDistance(currentSlot, i);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestSlot = i;
                }
            }
        }
        
        return closestSlot;
    }
    
    /**
     * Find empty bucket in hotbar
     * @return slot index or -1 if not found
     */
    public static int findEmptyBucket(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.BUCKET) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Calculate circular distance between two hotbar slots
     */
    private static int getSlotDistance(int from, int to) {
        int forward = (to - from + 9) % 9;
        int backward = (from - to + 9) % 9;
        return Math.min(forward, backward);
    }
    
    /**
     * Check if player has any water bucket in hotbar
     */
    public static boolean hasWaterBucket(ClientPlayerEntity player) {
        return findWaterBucket(player) != -1;
    }
}