package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.ModItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
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

@Mixin(AxolotlEntity.class)
public abstract class AxolotlEntityMixin extends AnimalEntity {
    protected AxolotlEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
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
            AxolotlEntity axolotl = (AxolotlEntity) (Object) this;
            NbtCompound fullNbt = new NbtCompound();
            axolotl.saveNbt(fullNbt);

            NbtCompound cleanNbt = new NbtCompound();
            cleanNbt.putString("id", EntityType.getId(EntityType.AXOLOTL).toString());
            if (fullNbt.contains("Brain")) cleanNbt.put("Brain", fullNbt.get("Brain"));
            if (fullNbt.contains("Variant")) cleanNbt.putInt("Variant", fullNbt.getInt("Variant").get());
            if (fullNbt.contains("Age")) cleanNbt.putInt("Age", fullNbt.getInt("Age").get());
            if (fullNbt.contains("Health")) cleanNbt.putFloat("Health", fullNbt.getFloat("Health").get());
            if (fullNbt.contains("CustomName")) cleanNbt.putString("CustomName", fullNbt.getString("CustomName").get());
            if (fullNbt.contains("CustomNameVisible")) cleanNbt.putBoolean("CustomNameVisible", fullNbt.getBoolean("CustomNameVisible").get());
            if (fullNbt.contains("PersistenceRequired")) cleanNbt.putBoolean("PersistenceRequired", fullNbt.getBoolean("PersistenceRequired").get());
            cleanNbt.putBoolean("FromBucket", true);
            if (fullNbt.contains("Brain")) {
                NbtCompound originalBrain = fullNbt.getCompound("Brain").get();
                if (originalBrain.contains("memories")) {
                    NbtCompound originalMemories = originalBrain.getCompound("memories").get();
                    NbtCompound cleanMemories = new NbtCompound();

                    String attackCooldownKey = "minecraft:has_hunting_cooldown";
                    String playDeadKey = "minecraft:play_dead_ticks";

                    if (originalMemories.contains(attackCooldownKey)) {
                        cleanMemories.put(attackCooldownKey, originalMemories.get(attackCooldownKey).copy());
                    }
                    if (originalMemories.contains(playDeadKey)) {
                        cleanMemories.put(playDeadKey, originalMemories.get(playDeadKey).copy());
                    }

                    if (!cleanMemories.isEmpty()) {
                        NbtCompound cleanBrain = new NbtCompound();
                        cleanBrain.put("memories", cleanMemories);
                        cleanNbt.put("Brain", cleanBrain);
                    }
                }
            }

            entities.add(cleanNbt);
            stack.set(ENTITIES_IN_BUCKET, entities);
            int size = stack.getOrDefault(ENTITIES_SIZE, 0);
            stack.set(ENTITIES_SIZE, ++size);

            player.playSound(SoundEvents.ITEM_BUCKET_FILL_AXOLOTL, 1.0F, 1.0F);
            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

            axolotl.discard();

            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
