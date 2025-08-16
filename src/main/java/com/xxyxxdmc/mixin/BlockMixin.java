package com.xxyxxdmc.mixin;

import com.xxyxxdmc.function.ChainMineState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "dropStack(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onDropStack(World world, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (ChainMineState.isChainMining()) {
            ChainMineState.captureDrop(stack);
            ci.cancel();
        }
    }
    @Inject(
        method = "dropExperienceWhenMined",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;dropExperience(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;I)V"
        ),
        cancellable = true
    )
    private static void onDropExperience(ServerWorld world, BlockPos pos, ItemStack stack, int amount, CallbackInfo ci) {
        if (ChainMineState.isChainMining()) {
            ChainMineState.addCapturedXp(amount);
            ci.cancel();
        }
    }

}

