package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.ModItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
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
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isOf(ModItem.LARGE_BUCKET)
                && stack.getOrDefault(FLUID_TYPE, 0) == 1
                && stack.getOrDefault(MODE, 1) == 1
                && stack.getOrDefault(WATER_CAPACITY, 0) - stack.getOrDefault(ENTITIES_SIZE, 0) > 0) {
            List<NbtCompound> entities = new ArrayList<>(stack.getOrDefault(ENTITIES_IN_BUCKET, List.of()));
            FishEntity fish = (FishEntity) (Object) this;
            NbtCompound cleanNbt = new NbtCompound();
            NbtCompound fullNbt = new NbtCompound();
            fish.saveNbt(fullNbt);
            cleanNbt.putString("id", EntityType.getId(this.getType()).toString());
            cleanNbt.putFloat("Health", fish.getHealth());
            if (fish.hasCustomName()) {
                cleanNbt.putString("CustomName", fish.getCustomName().getString());
            }
            if (this.isPersistent()) {
                cleanNbt.putBoolean("PersistenceRequired", true);
            }

            if (fish instanceof TropicalFishEntity) {
                if (fullNbt.contains("Variant")) {
                    cleanNbt.put("Variant", fullNbt.get("Variant").copy());
                }
            } else if (fish instanceof PufferfishEntity pufferFish) {
                cleanNbt.putInt("PuffState", pufferFish.getPuffState());
            }

            cleanNbt.putBoolean("FromBucket", true);
            entities.add(cleanNbt);
            stack.set(ENTITIES_IN_BUCKET, entities);
            int size = stack.getOrDefault(ENTITIES_SIZE, 0);
            stack.set(ENTITIES_SIZE, ++size);

            player.playSound(SoundEvents.ITEM_BUCKET_FILL_FISH, 1.0F, 1.0F);
            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

            fish.discard();

            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
