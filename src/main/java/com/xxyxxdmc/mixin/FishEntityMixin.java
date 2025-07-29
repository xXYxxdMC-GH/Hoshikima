package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.LargeBucketInteractionHelper;
import com.xxyxxdmc.init.ModItem;
import com.xxyxxdmc.init.other.LargeBucketEcoSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static com.xxyxxdmc.init.ModDataComponents.*;

@Mixin(FishEntity.class)
public abstract class FishEntityMixin extends WaterCreatureEntity {
    protected FishEntityMixin(EntityType<? extends WaterCreatureEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteractWithLargeBucket(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ActionResult result = LargeBucketInteractionHelper.tryPickup(
                (FishEntity) (Object) this,
                player,
                hand,
                SoundEvents.ITEM_BUCKET_FILL_FISH
        );

        if (result.isAccepted()) {
            cir.setReturnValue(result);
        }
    }
}
