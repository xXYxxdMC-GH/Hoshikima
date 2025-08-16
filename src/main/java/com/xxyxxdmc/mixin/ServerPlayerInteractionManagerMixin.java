package com.xxyxxdmc.mixin;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.function.ChainMineState;
import com.xxyxxdmc.init.callback.IChainMineState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.*;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow private ServerPlayerEntity player;
    @Shadow private ServerWorld world;

    private static final HoshikimaConfig config = HoshikimaConfig.get();

    private static final int MAX_BLOCKS_TO_BREAK = config.blockChainLimit;

    @Redirect(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;afterBreak(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    private void onAfterBlockBroken(Block instance, World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        IChainMineState playerState = (IChainMineState) this.player;
        if (!playerState.isChainMiningActive()) {
            instance.afterBreak(world, player, pos, state, blockEntity, tool);
            return;
        }

        Block originalBlock = state.getBlock();

        ChainMineState.setChainMining(true);
        ChainMineState.clearCapturedDrops();
        ChainMineState.clearCapturedXp();
        try {
            switch (config.chainMode) {
                case 0 :
                    findAndBreakConnectedBlocks(pos, originalBlock);
                    break;
                case 1 :
                    locateAndBreakStringBlocks(pos);
                    break;
                default :
                    break;
            }
        } finally {
            ChainMineState.setChainMining(false);
            for (ItemStack drop : ChainMineState.getCapturedDrops()) {
                Block.dropStack(this.world, pos, drop);
            }
            int totalXp = ChainMineState.getCapturedXp();
            if (totalXp > 0) {
                ExperienceOrbEntity.spawn(this.world, pos.toCenterPos(), totalXp);
            }
            ChainMineState.clearCapturedDrops();
            ChainMineState.clearCapturedXp();
        }
    }

    private void locateAndBreakStringBlocks(BlockPos pos) {
        Direction oppDirection = getTargetedFace(this.world, this.player).getOpposite();
        int breakedBlock = 0;
        ItemStack mainHandStack = this.player.  getMainHandStack();
        int dx = oppDirection.getOffsetX();
        int dy = oppDirection.getOffsetY();
        int dz = oppDirection.getOffsetZ();
        for (int i = 0;breakedBlock < MAX_BLOCKS_TO_BREAK;i++,breakedBlock++){
            BlockState block = this.world.getBlockState(pos.add(dx * i, dy * i, dz * i));
            if (block.isAir()) {
                breakedBlock--;
                continue;
            }
            if (block.getHardness(world, pos) < 0) return;
            if (mainHandStack.isDamageable() && mainHandStack.getDamage() >= mainHandStack.getMaxDamage() - 1) break;
            this.world.breakBlock(pos, true, this.player);
            if (!this.player.isCreative()) {                    mainHandStack.postMine(this.world, this.world.getBlockState(pos), pos, this.player);                                  
            }
        } 
    }

    private void findAndBreakConnectedBlocks(BlockPos startPos, Block originalBlock) {
        List<BlockPos> blocksToBreak = findConnectedBlocks(startPos, originalBlock);

        int blocksBroken = 0;
        ItemStack mainHandStack = this.player.getMainHandStack();

        for (BlockPos currentPos : blocksToBreak) {
            if (blocksBroken >= MAX_BLOCKS_TO_BREAK) break;

            if (mainHandStack.isDamageable() && mainHandStack.getDamage() >= mainHandStack.getMaxDamage() - 1) {
                break;
            }

            this.world.breakBlock(currentPos, true, this.player);

            if (!this.player.isCreative()) {
                mainHandStack.postMine(this.world, this.world.getBlockState(currentPos), currentPos, this.player);
            }
            blocksBroken++;
        }
    }

    private List<BlockPos> findConnectedBlocks(BlockPos startPos, Block originalBlock) {
        List<BlockPos> foundBlocks = new ArrayList<>();
        Queue<BlockPos> blocksToVisit = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        addNeighborsToQueue(startPos, blocksToVisit, visited);

        while (!blocksToVisit.isEmpty() && foundBlocks.size() < MAX_BLOCKS_TO_BREAK) {
            BlockPos currentPos = blocksToVisit.poll();

            if (this.world.getBlockState(currentPos).isOf(originalBlock)) {
                foundBlocks.add(currentPos);
                addNeighborsToQueue(currentPos, blocksToVisit, visited);
            }
        }
        return foundBlocks;
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

    public static Direction getTargetedFace(World world, PlayerEntity player) {
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d reachEnd = eyePos.add(lookVec.multiply(player.getAttributes().getValue(EntityAttributes.BLOCK_INTERACTION_RANGE)));

        RaycastContext context = new RaycastContext(
            eyePos,
            reachEnd,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );

        HitResult result = world.raycast(context);

        if (result instanceof BlockHitResult blockHit) {
            return blockHit.getSide();
        }

        return null;
    }
}
