package com.xxyxxdmc.mixin;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.function.ChainMineState;
import com.xxyxxdmc.init.api.IChainMineState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow @Final private ServerPlayerEntity player;
    @Shadow @Final private ServerWorld world;

    private static final HoshikimaConfig config = HoshikimaConfig.get();

    @Inject(
            method = "tryBreakBlock",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = this.world.getBlockState(pos);
        Block originalBlock = blockState.getBlock();
        //originalBlock.afterBreak(this.world, this.player, pos, blockState, this.world.getBlockEntity(pos), this.player.getActiveItem());
        IChainMineState playerState = (IChainMineState) this.player;
        if (!playerState.isChainMiningActive()) {
            return;
        }

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
        cir.setReturnValue(true);
        cir.cancel();
    }
    
    private void breakPendingBlocks() {
        IChainMineState state = (IChainMineState) this.player;
        List<BlockPos> blocksToBreak = state.getPendingBreakList();

        if (blocksToBreak == null || blocksToBreak.isEmpty()) return;

        int broken = 0;
        ItemStack mainHandStack = this.player.getMainHandStack();

        for (BlockPos pos : blocksToBreak) {
            if (broken > config.blockChainLimit) break;
            if (mainHandStack.isDamageable() && mainHandStack.getDamage() + config.antiToolBreakValue >= mainHandStack.getMaxDamage() - 1) {
                player.sendMessage(Text.translatable("massage.hoshikima.chain.mine.anti.tool.break"), true);
                break;
            }
            BlockState blockState = this.world.getBlockState(pos);
            if (blockState.getHardness(world, pos) < 0 && !player.isCreative()) break;

            this.world.breakBlock(pos, (!this.player.isCreative() && this.player.canHarvest(blockState)), this.player);

            if (!player.isCreative()) {
                mainHandStack.postMine(world, blockState, pos, player);
            }

            broken++;
        }

        state.clearPendingBreakList();
    }
}
