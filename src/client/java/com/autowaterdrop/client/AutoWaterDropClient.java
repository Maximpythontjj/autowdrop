package com.autowaterdrop.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import com.autowaterdrop.client.util.LatencyUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import com.autowaterdrop.client.config.AwdConfig;
import com.autowaterdrop.client.hud.AwdHud;
import com.autowaterdrop.client.util.InventoryUtil;
import com.autowaterdrop.client.util.PlacementUtil;

public class AutoWaterDropClient implements ClientModInitializer {
    private static final double FALL_VELOCITY_THRESHOLD = -0.5; // blocks/tick

    private double fallStartY = Double.NaN;
    private int ticksUntilPlace = -1;
    private int previousSelectedSlot = -1;
    private boolean scheduledPickup = false;
    private int pickupDelayTicks = 0;
    private boolean wasOnGround = true;

    @Override
    public void onInitializeClient() {
        AwdConfig.load();

        KeyBinding cfgKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autowaterdrop.config",
                GLFW.GLFW_KEY_O,
                "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }

            final ClientPlayerEntity player = client.player;

            // Exclusions: creative, spectator, elytra, mount
            if (!player.isAlive() || player.isCreative() || player.isSpectator() || player.isGliding() || player.hasVehicle()) {
                resetState();
                return;
            }

            // Ready HUD pre-calculation: do we have water bucket in hotbar?
            boolean hasBucket = InventoryUtil.findBestWaterBucketSlot(player.getInventory(), player.getInventory().selectedSlot) != -1;

            // Raycast to compute valid surface for HUD readiness
            boolean hasSurface = false;
            if (client.world != null) {
                BlockHitResult hit = raycastDown(player, 256.0);
                hasSurface = hit != null && hit.getType() == HitResult.Type.BLOCK;
            }
            AwdHud.setReady(hasBucket && hasSurface && !player.isTouchingWater());

            boolean onGround = player.isOnGround();

            // Open config on key press
            while (cfgKey.wasPressed()) {
                client.setScreen(new com.autowaterdrop.client.config.AwdConfigScreen(client.currentScreen));
            }
            double vy = player.getVelocity().y;

            if (onGround) {
                fallStartY = Double.NaN;
                wasOnGround = true;
            } else {
                if (wasOnGround) {
                    fallStartY = player.getY();
                }
                wasOnGround = false;
            }

            // If not falling meaningfully, clear schedule
            if (onGround || vy >= FALL_VELOCITY_THRESHOLD) {
                ticksUntilPlace = -1;
            }

            // Determine fall distance
            double fallDistance = (Double.isNaN(fallStartY) ? 0.0 : (fallStartY - player.getY()));

            // Early exit if not above threshold
            if (fallDistance < AwdConfig.get().minFallHeightBlocks) {
                handleDeferredPickup(client);
                return;
            }

            // Must have water bucket in hotbar
            int bestBucketSlot = InventoryUtil.findBestWaterBucketSlot(player.getInventory(), player.getInventory().selectedSlot);
            if (bestBucketSlot == -1) {
                handleDeferredPickup(client);
                return;
            }

            // If already in water, or in web/slime cancel
            if (player.isTouchingWater() || player.isClimbing()) {
                resetState();
                return;
            }

            // Predict impact time and schedule placement
            BlockHitResult ground = raycastDown(player, 256.0);
            if (ground == null || ground.getType() != HitResult.Type.BLOCK) {
                handleDeferredPickup(client);
                return;
            }

            double distanceToGround = Math.max(0.0, player.getY() - ground.getPos().y);
            // Estimate ticks to impact using current vy and gravity approx per tick
            // Use simple kinematics with mc gravity ~ -0.08 and velocity dampening 0.98; however, for safety we approximate linearly
            int estimatedTicksToImpact = estimateTicksToImpact(distanceToGround, vy);

            int activationLead = AwdConfig.get().activationLeadTicks;
            if (AwdConfig.get().safeModeExtraTick) activationLead += 1;
            activationLead += getLatencySafetyTicks(client);

            if (estimatedTicksToImpact <= activationLead) {
                // Execute placement now
                if (previousSelectedSlot == -1 && AwdConfig.get().restorePreviousSlot) {
                    previousSelectedSlot = player.getInventory().selectedSlot;
                }

                if (player.getInventory().selectedSlot != bestBucketSlot) {
                    player.getInventory().selectedSlot = bestBucketSlot;
                }

                // Try place underfoot or nearest valid neighbor
                boolean placed = PlacementUtil.tryPlaceWater(client, player, ground.getBlockPos());
                if (placed) {
                    if (AwdConfig.get().showChatOnActivate) {
                        player.sendMessage(Text.literal("Auto WaterDrop: activated"), false);
                    }
                    if (AwdConfig.get().autoPickupTicks > 0) {
                        scheduledPickup = true;
                        pickupDelayTicks = AwdConfig.get().autoPickupTicks;
                    }
                    ticksUntilPlace = -1;
                }
            } else {
                ticksUntilPlace = estimatedTicksToImpact - activationLead;
            }

            handleDeferredPickup(client);
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            AwdHud.render(context);
        });
    }

    private void handleDeferredPickup(MinecraftClient client) {
        if (scheduledPickup) {
            if (pickupDelayTicks > 0) {
                pickupDelayTicks--;
            } else {
                // Attempt to pick water back up by using the water bucket again
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                }
                scheduledPickup = false;
                if (AwdConfig.get().restorePreviousSlot && previousSelectedSlot != -1 && client.player != null) {
                    client.player.getInventory().selectedSlot = previousSelectedSlot;
                }
                previousSelectedSlot = -1;
            }
        }
    }

    private void resetState() {
        ticksUntilPlace = -1;
        scheduledPickup = false;
        pickupDelayTicks = 0;
        previousSelectedSlot = -1;
    }

    private static BlockHitResult raycastDown(ClientPlayerEntity player, double maxDistance) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d end = start.add(0, -maxDistance, 0);
        RaycastContext ctx = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, player);
        return player.getWorld().raycast(ctx);
    }

    private static int estimateTicksToImpact(double distance, double velocityY) {
        // Conservative: assume current speed continues, but clamp to at least 1 tick
        if (distance <= 0) return 0;
        double speed = Math.abs(velocityY);
        if (speed < 0.01) speed = 0.01;
        int linearEstimate = (int)Math.ceil(distance / speed);
        // Add small buffer to account for acceleration
        return Math.max(1, linearEstimate - 1);
    }

    private static int getLatencySafetyTicks(MinecraftClient client) {
        return LatencyUtil.getLatencyTicks(client);
    }
}

