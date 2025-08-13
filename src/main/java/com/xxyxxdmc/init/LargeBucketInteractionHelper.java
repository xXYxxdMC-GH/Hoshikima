package com.xxyxxdmc.init;

import com.xxyxxdmc.init.other.LargeBucketEcoSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static com.xxyxxdmc.init.ModDataComponents.*;

public class LargeBucketInteractionHelper {
    public static ActionResult tryPickup(LivingEntity entity, PlayerEntity player, Hand hand, SoundEvent pickupSound) {
        ItemStack stack = player.getStackInHand(hand);

        boolean canPickup = stack.isOf(ModItem.LargeBucketRegister.LARGE_BUCKET)
                && stack.getOrDefault(FLUID_TYPE, 0) == 1
                && stack.getOrDefault(MODE, 1) == 1
                && stack.getOrDefault(WATER_CAPACITY, 0) > stack.getOrDefault(ENTITIES_SIZE, 0);

        if (!canPickup) {
            return ActionResult.PASS;
        }

        World world = entity.getWorld();
        if (!world.isClient()) {
            List<NbtCompound> currentEntities = stack.getOrDefault(ENTITIES_IN_BUCKET, List.of());
            List<NbtCompound> realEntities = new ArrayList<>(
                    currentEntities.stream()
                            .filter(nbt -> !LargeBucketEcoSystem.isEmpty(nbt))
                            .toList()
            );

            NbtCompound entityNbt = createNbtFrom(entity);
            realEntities.add(entityNbt);

            stack.set(ENTITIES_IN_BUCKET, realEntities);
            stack.set(ENTITIES_SIZE, realEntities.size());

            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            entity.discard();
        }

        player.playSound(pickupSound, 1.0F, 1.0F);

        return ActionResult.SUCCESS;
    }

    private static NbtCompound createNbtFrom(LivingEntity entity) {
        NbtCompound nbt = new NbtCompound();
        entity.saveNbt(nbt);
        nbt.remove("Pos");
        nbt.remove("Motion");
        nbt.remove("Rotation");
        nbt.remove("UUID");
        nbt.remove("CanPickUpLoot");
        nbt.remove("DeathTime");
        nbt.remove("FallFlying");
        nbt.remove("Fire");
        nbt.remove("HurtTime");
        nbt.remove("Invulnerable");
        nbt.remove("LeftHanded");
        nbt.remove("OnGround");
        nbt.remove("PortalCooldown");
        nbt.remove("fall_distance");
        nbt.putString("id", EntityType.getId(entity.getType()).toString());
        nbt.putBoolean("FromBucket", true);
        return nbt;
    }
}
