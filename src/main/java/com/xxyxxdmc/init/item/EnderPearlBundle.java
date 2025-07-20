package com.xxyxxdmc.init.item;

import com.xxyxxdmc.init.entity.SpecialEnderPearlEntity;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.awt.*;
import java.util.function.Consumer;

import static com.xxyxxdmc.init.ModDataComponents.*;

public class EnderPearlBundle extends Item {
    public final int maxCount = 1728;
    public EnderPearlBundle(Item.Settings settings){
        super(settings.maxCount(1));
    }
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        ItemStack itemStack = user.getStackInHand(hand);
        int count = itemStack.getOrDefault(COUNT, 0);
        if (count > 0) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            if (world instanceof ServerWorld serverWorld) {
                ProjectileEntity.spawnWithVelocity(SpecialEnderPearlEntity::new, serverWorld, Items.ENDER_PEARL.getDefaultStack(), user, 0.0F, 1.5F, 1.0F);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            itemStack.set(COUNT, --count);
            user.getItemCooldownManager().set(itemStack, 20);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isOf(Items.ENDER_PEARL)) {
            int currentCount = stack.getOrDefault(COUNT, 0);
            int space = maxCount - currentCount;
            if (space > 0) {
                int toAdd = Math.min(space, otherStack.getCount());
                stack.set(COUNT, currentCount + toAdd);
                otherStack.decrement(toAdd);
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1.0F, 1.0F);
            } else {
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
            }
            return true;
        } else if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            int currentCount = stack.getOrDefault(COUNT, 0);
            int count = Math.min(currentCount, 16);
            if (currentCount >= 0) {
                stack.set(COUNT, currentCount - count);
                player.currentScreenHandler.setCursorStack(new ItemStack(Items.ENDER_PEARL, count));
                player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    public int getEnderPearlCount(ItemStack stack) {
        return stack.getComponents().getOrDefault(COUNT, 0);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * getEnderPearlCount(stack) / (float) this.maxCount);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return new Color(132, 0, 255).getRGB();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        int currentCount = getEnderPearlCount(stack);
        textConsumer.accept(Text.translatable("tooltip.randomthing.count")
                .append(Text.literal(": " + currentCount + " / " + this.maxCount))
                .formatted(Formatting.GRAY));
    }
}
