package com.autowaterdrop.client.util;

import com.autowaterdrop.client.config.AwdConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class PlacementUtil {
    private PlacementUtil() {}

    public static boolean tryPlaceWater(MinecraftClient client, ClientPlayerEntity player, BlockPos basePos) {
        if (client.interactionManager == null) return false;

        // Ensure player actually holds a water bucket
        ItemStack held = player.getMainHandStack();
        if (held == null || held.getItem() != Items.WATER_BUCKET) {
            // attempt with offhand if present
            held = player.getOffHandStack();
            if (held == null || held.getItem() != Items.WATER_BUCKET) return false;
        }

        // First try directly above target block
        if (canPlaceAt(player, basePos.up())) {
            if (useOnBlock(client, player, basePos, Direction.UP)) return true;
        }

        if (!AwdConfig.get().searchNeighborPlacements) return false;

        // Try neighbors around basePos to handle thin/unplaceable blocks underfoot
        BlockPos[] offsets = new BlockPos[] {
            new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(-1, 0, 1), new BlockPos(-1, 0, -1)
        };
        for (BlockPos off : offsets) {
            BlockPos candidate = basePos.add(off).up();
            if (canPlaceAt(player, candidate)) {
                // face towards center of candidate
                if (useOnBlock(client, player, candidate.down(), Direction.UP)) return true;
            }
        }
        return false;
    }

    private static boolean canPlaceAt(ClientPlayerEntity player, BlockPos pos) {
        BlockState state = player.getWorld().getBlockState(pos);
        // Allow replacing air and water (for update), avoid solid blocks occupying the space
        return state.isAir() || state.isOf(Blocks.WATER) || state.getFluidState().isEmpty();
    }

    private static boolean useOnBlock(MinecraftClient client, ClientPlayerEntity player, BlockPos target, Direction face) {
        Vec3d hitVec = Vec3d.ofCenter(target).add(0, -0.49, 0);
        BlockHitResult bhr = new BlockHitResult(hitVec, face, target, false);
        ActionResult res = client.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
        if (res.isAccepted()) return true;
        // Try offhand as fallback
        res = client.interactionManager.interactBlock(player, Hand.OFF_HAND, bhr);
        return res.isAccepted();
    }
}

