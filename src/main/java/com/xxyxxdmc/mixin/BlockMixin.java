package com.xxyxxdmc.mixin;

import com.xxyxxdmc.function.ChainMineState;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "dropStack(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onDropStack(World world, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (ChainMineState.isChainMining()) {
            ChainMineState.captureDrop(stack);
            ci.cancel();
        }
    }
    @Inject(method = "dropExperience(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;I)V", at = @At("HEAD"), cancellable = true)
    private static void onDropExperience(ServerWorld world, BlockPos pos, int size, CallbackInfo ci) {
        if (ChainMineState.isChainMining()) {
            ChainMineState.addCapturedXp(size);
            ci.cancel();
        }
    }
}
