package com.autowaterdrop.client.util;

import net.minecraft.block.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

public class BlockValidator {
    
    /**
     * Check if water can be placed at the given position
     */
    public static boolean canPlaceWaterAt(ClientWorld world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        
        // Check if position is already water
        if (state.getFluidState().getFluid() == Fluids.WATER) {
            return false;
        }
        
        // Check if block is replaceable
        if (!state.isReplaceable()) {
            return false;
        }
        
        // Check for blocks that prevent water placement
        if (isInvalidForWaterPlacement(block, state)) {
            return false;
        }
        
        // Check the block below - water needs solid support or another water source
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);
        
        // Allow placement on solid blocks or in water
        return belowState.isSolidBlock(world, belowPos) || 
               belowState.getFluidState().getFluid() == Fluids.WATER ||
               canWaterFlowInto(belowState);
    }
    
    /**
     * Check if a block prevents water placement
     */
    private static boolean isInvalidForWaterPlacement(Block block, BlockState state) {
        // List of blocks that prevent water placement
        return block instanceof TorchBlock ||
               block instanceof WallTorchBlock ||
               block instanceof SignBlock ||
               block instanceof WallSignBlock ||
               block instanceof HangingSignBlock ||
               block instanceof BannerBlock ||
               block instanceof WallBannerBlock ||
               block instanceof AbstractRedstoneGateBlock ||
               block instanceof RailBlock ||
               block instanceof LeverBlock ||
               block instanceof ButtonBlock ||
               block instanceof TripwireHookBlock ||
               (block instanceof FlowerBlock && !(block instanceof SeagrassBlock)) ||
               block instanceof SaplingBlock ||
               block instanceof CarpetBlock;
    }
    
    /**
     * Check if water can flow into this block
     */
    private static boolean canWaterFlowInto(BlockState state) {
        Block block = state.getBlock();
        
        // Water can flow through these blocks
        return state.isAir() ||
               block instanceof FireBlock ||
               block instanceof SnowBlock ||
               state.isIn(BlockTags.FLOWERS) ||
               block instanceof TallPlantBlock ||
               block instanceof DeadBushBlock ||
               block instanceof SeagrassBlock ||
               block instanceof TallSeagrassBlock;
    }
    
    /**
     * Find the nearest solid block below the given position
     */
    public static BlockPos findGroundBelow(ClientWorld world, BlockPos startPos, int maxDistance) {
        BlockPos.Mutable pos = startPos.mutableCopy();
        
        for (int i = 0; i < maxDistance; i++) {
            pos.move(0, -1, 0);
            BlockState state = world.getBlockState(pos);
            
            if (state.isSolidBlock(world, pos) || !state.getCollisionShape(world, pos).isEmpty()) {
                return pos.toImmutable();
            }
        }
        
        return null;
    }
    
    /**
     * Check if a position is safe for landing (has water or other safe blocks)
     */
    public static boolean isSafeLanding(ClientWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        
        // Check for water
        if (state.getFluidState().getFluid() == Fluids.WATER) {
            return true;
        }
        
        // Check for other safe landing blocks
        Block block = state.getBlock();
        return block instanceof SlimeBlock ||
               block instanceof HoneyBlock ||
               block instanceof CobwebBlock ||
               block instanceof PowderSnowBlock;
    }
}