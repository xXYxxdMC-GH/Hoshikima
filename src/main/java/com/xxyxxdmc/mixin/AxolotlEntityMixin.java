package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.LargeBucketInteractionHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxolotlEntity.class)
public abstract class AxolotlEntityMixin extends AnimalEntity {
    protected AxolotlEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteractWithLargeBucket(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = LargeBucketInteractionHelper.tryPickup(
                (AxolotlEntity) (Object) this,
                player,
                hand,
                SoundEvents.ITEM_BUCKET_FILL_AXOLOTL
        );

        if (result.isAccepted()) {
            cir.setReturnValue(result);
        }
    }
}
