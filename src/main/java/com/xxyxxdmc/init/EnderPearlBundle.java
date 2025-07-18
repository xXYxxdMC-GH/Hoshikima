package com.xxyxxdmc.init;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EnderPearlBundle extends Item {
    public static float POWER = 1.5F;
    public EnderPearlBundle(Item.Settings settings){
        super(settings.maxCount(1).maxDamage(16384));
    }
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.isSneaking()) itemStack.setDamage(itemStack.getMaxDamage() - 1);
        if (itemStack.getDamage() < itemStack.getMaxDamage() - 1 && !user.isSneaking()) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!world.isClient()) {
                SpecialEnderPearlEntity pearlEntity = new SpecialEnderPearlEntity(world, user, itemStack);
                pearlEntity.setVelocity();
                pearlEntity.setProperties(user, user.getPitch(), user.getYaw(), 0.0F, POWER, 1.0F);
                world.spawnEntity(pearlEntity);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            itemStack.damage(1, user);
            user.getItemCooldownManager().set(itemStack, 20);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
