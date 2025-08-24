package com.autowaterdrop.client.feature;

import com.autowaterdrop.client.AutoWaterDropClient;
import com.autowaterdrop.client.config.AutoWaterDropConfig;
import com.autowaterdrop.client.util.BlockValidator;
import com.autowaterdrop.client.util.InventoryUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class WaterDropManager {
    private final AutoWaterDropClient modClient;
    private boolean isFalling = false;
    private double fallStartY = 0;
    private int ticksUntilImpact = -1;
    private int previousSlot = -1;
    private boolean hasPlacedWater = false;
    private BlockPos waterPlacedPos = null;
    private int waterPickupTimer = 0;
    private int lastWaterBucketSlot = -1;
    
    // State tracking
    private boolean wasOnGround = true;
    private int fallTicks = 0;
    
    public WaterDropManager(AutoWaterDropClient modClient) {
        this.modClient = modClient;
    }
    
    public void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }
        
        AutoWaterDropConfig config = modClient.getConfig();
        
        // Handle water pickup timer
        if (waterPickupTimer > 0) {
            waterPickupTimer--;
            if (waterPickupTimer == 0 && config.isAutoPickupWater() && waterPlacedPos != null) {
                pickupWater(client, player);
            }
        }
        
        // Check if we should skip (Creative, Spectator, Elytra, Mount)
        if (shouldSkipWaterDrop(player)) {
            resetState();
            return;
        }
        
        // Check for water bucket in hotbar
        int waterBucketSlot = InventoryUtil.findWaterBucket(player);
        if (waterBucketSlot == -1) {
            lastWaterBucketSlot = -1;
            if (isFalling) {
                resetState();
            }
            return;
        }
        lastWaterBucketSlot = waterBucketSlot;
        
        // Detect falling state
        boolean currentlyOnGround = player.isOnGround();
        
        if (wasOnGround && !currentlyOnGround && player.getVelocity().y < -0.1) {
            // Just started falling
            if (!isFalling) {
                isFalling = true;
                fallStartY = player.getY();
                fallTicks = 0;
            }
        }
        
        if (isFalling) {
            fallTicks++;
            
            // Check if we've landed
            if (currentlyOnGround) {
                handleLanding(client, player);
                resetState();
                wasOnGround = true;
                return;
            }
            
            // Calculate fall distance and check if we need to act
            double fallDistance = fallStartY - player.getY();
            
            // Only proceed if we're actually falling fast enough and far enough
            if (player.getVelocity().y < -0.5 && fallDistance > 2) {
                // Predict landing
                LandingPrediction prediction = predictLanding(client, player);
                
                if (prediction != null && prediction.isValid()) {
                    double impactDistance = player.getY() - prediction.impactY;
                    
                    // Check if total fall height will exceed threshold
                    double totalFallHeight = fallStartY - prediction.impactY;
                    if (totalFallHeight >= config.getMinFallHeight()) {
                        // Calculate ticks until impact
                        int ticksToImpact = calculateTicksToImpact(player, impactDistance);
                        
                        // Check if it's time to place water
                        int placementTiming = config.getEffectivePlacementTicks();
                        
                        if (ticksToImpact <= placementTiming && !hasPlacedWater) {
                            placeWater(client, player, prediction, waterBucketSlot);
                        }
                    }
                }
            }
        }
        
        wasOnGround = currentlyOnGround;
    }
    
    private boolean shouldSkipWaterDrop(ClientPlayerEntity player) {
        return player.isCreative() || 
               player.isSpectator() || 
               player.isGliding() || 
               player.hasVehicle() ||
               player.isTouchingWater() ||
               player.isInLava();
    }
    
    private LandingPrediction predictLanding(MinecraftClient client, ClientPlayerEntity player) {
        Vec3d startPos = player.getPos();
        Vec3d velocity = player.getVelocity();
        
        // Raycast downward to find landing spot
        Vec3d endPos = startPos.add(0, -256, 0);
        RaycastContext context = new RaycastContext(
            startPos,
            endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.SOURCE_ONLY,
            player
        );
        
        BlockHitResult hitResult = client.world.raycast(context);
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos landingPos = hitResult.getBlockPos();
            double impactY = landingPos.getY() + 1.0;
            
            // Check if landing position is valid for water placement
            BlockPos waterPos = landingPos.up();
            boolean canPlaceWater = BlockValidator.canPlaceWaterAt(client.world, waterPos);
            
            // If can't place directly, search nearby
            if (!canPlaceWater && modClient.getConfig().isSearchNearbyBlocks()) {
                waterPos = findNearbyValidPosition(client, waterPos);
                canPlaceWater = waterPos != null;
            }
            
            return new LandingPrediction(landingPos, impactY, waterPos, canPlaceWater);
        }
        
        return null;
    }
    
    private BlockPos findNearbyValidPosition(MinecraftClient client, BlockPos center) {
        // Search in order: direct adjacents, then diagonals
        BlockPos[] searchOrder = {
            center.north(),
            center.south(),
            center.east(),
            center.west(),
            center.north().east(),
            center.north().west(),
            center.south().east(),
            center.south().west()
        };
        
        for (BlockPos pos : searchOrder) {
            if (BlockValidator.canPlaceWaterAt(client.world, pos)) {
                return pos;
            }
        }
        
        return null;
    }
    
    private int calculateTicksToImpact(ClientPlayerEntity player, double distance) {
        double velocity = Math.abs(player.getVelocity().y);
        double gravity = 0.08; // Minecraft gravity
        double dragFactor = 0.98; // Air resistance
        
        int ticks = 0;
        double currentDistance = 0;
        double currentVelocity = velocity;
        
        while (currentDistance < distance && ticks < 100) {
            currentDistance += currentVelocity;
            currentVelocity = (currentVelocity + gravity) * dragFactor;
            ticks++;
        }
        
        return ticks;
    }
    
    private void placeWater(MinecraftClient client, ClientPlayerEntity player, 
                           LandingPrediction prediction, int waterBucketSlot) {
        if (!prediction.canPlaceWater || prediction.waterPos == null) {
            return;
        }
        
        // Save current slot
        previousSlot = player.getInventory().selectedSlot;
        
        // Switch to water bucket
        player.getInventory().selectedSlot = waterBucketSlot;
        client.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(waterBucketSlot));
        
        // Create block hit result for water placement
        Vec3d hitPos = Vec3d.ofCenter(prediction.waterPos.down());
        BlockHitResult blockHit = new BlockHitResult(
            hitPos,
            Direction.UP,
            prediction.waterPos.down(),
            false
        );
        
        // Place water
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, blockHit);
        
        hasPlacedWater = true;
        waterPlacedPos = prediction.waterPos;
        
        // Start pickup timer if enabled
        if (modClient.getConfig().isAutoPickupWater()) {
            waterPickupTimer = 2; // Wait 2 ticks before pickup
        }
        
        // Send notification if enabled
        if (modClient.getConfig().isShowNotifications()) {
            player.sendMessage(Text.literal("§b[Auto WaterDrop] §aActivated!"), true);
        }
        
        // Play sound if enabled
        if (modClient.getConfig().isPlaySounds()) {
            player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 0.5f, 1.0f);
        }
        
        AutoWaterDropClient.LOGGER.info("Water placed at {} to prevent fall damage", prediction.waterPos);
    }
    
    private void pickupWater(MinecraftClient client, ClientPlayerEntity player) {
        if (waterPlacedPos == null) return;
        
        // Check if we still have an empty bucket
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.getItem() != Items.BUCKET) {
            // Try to find empty bucket in hotbar
            int bucketSlot = InventoryUtil.findEmptyBucket(player);
            if (bucketSlot != -1) {
                player.getInventory().selectedSlot = bucketSlot;
                client.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(bucketSlot));
            } else {
                return;
            }
        }
        
        // Create hit result for water pickup
        Vec3d hitPos = Vec3d.ofCenter(waterPlacedPos);
        BlockHitResult blockHit = new BlockHitResult(
            hitPos,
            Direction.UP,
            waterPlacedPos,
            false
        );
        
        // Pick up water
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, blockHit);
        
        waterPlacedPos = null;
    }
    
    private void handleLanding(MinecraftClient client, ClientPlayerEntity player) {
        // Return to previous slot if enabled
        if (modClient.getConfig().isAutoReturnSlot() && previousSlot != -1) {
            player.getInventory().selectedSlot = previousSlot;
            client.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(previousSlot));
        }
    }
    
    private void resetState() {
        isFalling = false;
        fallStartY = 0;
        ticksUntilImpact = -1;
        hasPlacedWater = false;
        fallTicks = 0;
        if (waterPickupTimer == 0) {
            waterPlacedPos = null;
        }
    }
    
    public boolean isReady() {
        return lastWaterBucketSlot != -1;
    }
    
    public boolean isFalling() {
        return isFalling;
    }
    
    private static class LandingPrediction {
        final BlockPos landingPos;
        final double impactY;
        final BlockPos waterPos;
        final boolean canPlaceWater;
        
        LandingPrediction(BlockPos landingPos, double impactY, BlockPos waterPos, boolean canPlaceWater) {
            this.landingPos = landingPos;
            this.impactY = impactY;
            this.waterPos = waterPos;
            this.canPlaceWater = canPlaceWater;
        }
        
        boolean isValid() {
            return landingPos != null && canPlaceWater;
        }
    }
}