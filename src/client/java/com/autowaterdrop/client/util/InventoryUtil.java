package com.autowaterdrop.client.util;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class InventoryUtil {
    private InventoryUtil() {}

    public static int findBestWaterBucketSlot(PlayerInventory inv, int currentSelected) {
        int bestSlot = -1;
        int bestDistance = Integer.MAX_VALUE;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inv.getStack(slot);
            if (stack != null && stack.getItem() == Items.WATER_BUCKET) {
                int dist = ringDistance(slot, currentSelected);
                if (dist < bestDistance) {
                    bestDistance = dist;
                    bestSlot = slot;
                }
            }
        }
        return bestSlot;
    }

    private static int ringDistance(int a, int b) {
        int diff = Math.abs(a - b);
        return Math.min(diff, 9 - diff);
    }
}

