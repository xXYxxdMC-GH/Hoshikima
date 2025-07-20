package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.callback.ItemPickupCallback;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    public abstract ItemStack getStack();
    @Inject(method = "onPlayerCollision",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"),
            cancellable = true)
    private void injectItemPickupEvent(PlayerEntity player, CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        ItemStack originalStack = itemEntity.getStack();

        ItemStack stackToPickup = originalStack.copy();

        ActionResult result = ItemPickupCallback.EVENT.invoker().onItemPickup(player, itemEntity, stackToPickup);

        if (result == ActionResult.SUCCESS) {
            itemEntity.discard();
            ci.cancel();
        } else if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}
