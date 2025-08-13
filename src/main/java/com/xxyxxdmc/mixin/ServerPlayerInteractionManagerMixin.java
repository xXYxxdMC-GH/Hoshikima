package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.callback.IChainMineState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow public ServerPlayerEntity player;
    @Shadow public ServerWorld world;

    private static final int MAX_BLOCKS_TO_BREAK = 128;

    private static final ThreadLocal<Boolean> isChainMining = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<List<ItemStack>> capturedDrops = ThreadLocal.withInitial(ArrayList::new);

    public static boolean isChainMining() {
        return isChainMining.get();
    }

    public static void captureDrop(ItemStack stack) {
        capturedDrops.get().add(stack);
    }

    @Inject(
            method = "tryBreakBlock",
            at = @At("TAIL")
    )
    private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        IChainMineState playerState = (IChainMineState) this.player;
        if (!playerState.hoshikima_isChainMiningActive()) {
            return;
        }

        BlockState originalState = this.world.getBlockState(pos);
        Block originalBlock = originalState.getBlock();

        boolean isChainable = originalState.isIn(BlockTags.LOGS) ||
                originalState.isIn(BlockTags.COAL_ORES) ||
                originalState.isIn(BlockTags.IRON_ORES) ||
                originalState.isIn(BlockTags.COPPER_ORES) ||
                originalState.isIn(BlockTags.GOLD_ORES) ||
                originalState.isIn(BlockTags.REDSTONE_ORES) ||
                originalState.isIn(BlockTags.LAPIS_ORES) ||
                originalState.isIn(BlockTags.DIAMOND_ORES) ||
                originalState.isIn(BlockTags.EMERALD_ORES);

        if (!isChainable) {
            return;
        }

        isChainMining.set(true);
        capturedDrops.get().clear();
        try {
            findAndBreakConnectedBlocks(pos, originalBlock);
        } finally {
            for (ItemStack drop : capturedDrops.get()) {
                if (!this.player.getInventory().insertStack(drop)) {
                    Block.dropStack(this.world, pos, drop);
                }
            }
            isChainMining.set(false);
            capturedDrops.get().clear();
        }
    }

    private void findAndBreakConnectedBlocks(BlockPos startPos, Block originalBlock) {
        Queue<BlockPos> blocksToVisit = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        blocksToVisit.add(startPos);
        visited.add(startPos);

        int blocksBroken = 0;
        ItemStack mainHandStack = this.player.getMainHandStack();

        blocksToVisit.poll();

        addNeighborsToQueue(startPos, blocksToVisit, visited);

        while (!blocksToVisit.isEmpty() && blocksBroken < MAX_BLOCKS_TO_BREAK) {
            BlockPos currentPos = blocksToVisit.poll();

            if (this.world.getBlockState(currentPos).isOf(originalBlock)) {
                if (mainHandStack.isDamageable() && mainHandStack.getDamage() >= mainHandStack.getMaxDamage() - 1) {
                    break;
                }

                this.world.breakBlock(currentPos, true, this.player);

                if (!this.player.isCreative()) {
                    mainHandStack.postMine(this.world, this.world.getBlockState(currentPos), currentPos, this.player);
                }

                blocksBroken++;
                addNeighborsToQueue(currentPos, blocksToVisit, visited);
            }
        }
    }

    private void addNeighborsToQueue(BlockPos pos, Queue<BlockPos> queue, Set<BlockPos> visited) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos neighborPos = pos.add(x, y, z);
                    if (visited.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                }
            }
        }
    }
}