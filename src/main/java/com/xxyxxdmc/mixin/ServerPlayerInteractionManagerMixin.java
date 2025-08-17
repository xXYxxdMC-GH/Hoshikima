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
            breakPendingBlocks();
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
    
    private void breakPendingBlocks() {
    IChainMineState state = (IChainMineState) this.player;
    List<BlockPos> blocksToBreak = state.getPendingBreakList();

    if (blocksToBreak == null || blocksToBreak.isEmpty()) return;

    int broken = 0;
    ItemStack mainHandStack = this.player.getMainHandStack();

    for (BlockPos pos : blocksToBreak) {
        if (broken >= MAX_BLOCKS_TO_BREAK) break;
        if (mainHandStack.isDamageable() && mainHandStack.getDamage() >= mainHandStack.getMaxDamage() - 1) break;

        this.world.breakBlock(pos, true, this.player);

        if (!this.player.isCreative()) {
            mainHandStack.postMine(this.world, this.world.getBlockState(pos), pos, this.player);
        }

        broken++;
    }

    state.clearPendingBreakList();
}
}
