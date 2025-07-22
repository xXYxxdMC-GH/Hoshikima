package com.xxyxxdmc.init.item;

import com.xxyxxdmc.init.ModDataComponents;
import com.xxyxxdmc.mixin.FireworkAccessor;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.awt.*;
import java.util.function.Consumer;

import static com.xxyxxdmc.init.ModDataComponents.*;

public class FireworkThruster extends Item {
    private final int maxFuel = 192;

    public FireworkThruster(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        stack.set(ModDataComponents.FUEL, this.maxFuel);
        return stack;
    }

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            int power = itemStack.getOrDefault(POWER, 1);
            if (power + 1 > 5) power = 1;
            else power++;
            itemStack.set(POWER, power);
            user.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1.0F, 1.0F);
            user.sendMessage(Text.translatable("tooltip.hoshikima.power").append(": ").append(Text.literal(String.valueOf(power))), true);
            return ActionResult.SUCCESS;
        } else if (user.isGliding()) {
            int currentFuel = itemStack.getOrDefault(FUEL, 0);
            int power = itemStack.getOrDefault(POWER, 1);
            if (currentFuel - power < 0 || itemStack.getOrDefault(MISSING_PAPER, true)) return ActionResult.PASS;
            if (world instanceof ServerWorld serverWorld) {
                FireworkRocketEntity entity = new FireworkRocketEntity(world, Items.FIREWORK_ROCKET.getDefaultStack(), user);
                int lifeTime = 10 * power + Random.create().nextInt(6) + Random.create().nextInt(7);
               ((FireworkAccessor) entity).setLifeTime(lifeTime);
                ProjectileEntity.spawn(entity, serverWorld, itemStack);
                itemStack.set(FUEL, currentFuel - power);
                user.incrementStat(Stats.USED.getOrCreateStat(this));
            }
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
            if (!stack.getOrDefault(MISSING_PAPER, true) && !otherStack.isOf(Items.PAPER)) {
                int currentFuel = stack.getOrDefault(FUEL, 0);
                if (otherStack.isOf(Items.GUNPOWDER)) {
                    if (currentFuel + 3 <= this.maxFuel) {
                        otherStack.decrement(1);
                        stack.set(FUEL, currentFuel + 3);
                        if (Math.random() < 0.03) stack.set(MISSING_PAPER, true);
                    }
                } else if (otherStack.isOf(Items.COAL)) {
                    if (currentFuel + 5 <= this.maxFuel) {
                        otherStack.decrement(1);
                        stack.set(FUEL, currentFuel + 5);
                        if (Math.random() < 0.1) stack.set(MISSING_PAPER, true);
                    }
                }
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1.0F, 1.0F);
            } else if (otherStack.isOf(Items.PAPER) && stack.getOrDefault(MISSING_PAPER, true)) {
                otherStack.decrement(1);
                if (Math.random() < 0.25) stack.set(MISSING_PAPER, false);
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1.0F, 1.0F);
            } else {
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !stack.getOrDefault(MISSING_PAPER, true);
    }

    public int getFuel(ItemStack stack) {
        return stack.getComponents().getOrDefault(FUEL, 0);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * getFuel(stack) / (float) this.maxFuel);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return new Color(0, 232, 189).getRGB();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int currentFuel = getFuel(stack);
        int power = stack.getOrDefault(POWER, 1);
        boolean missingPaper = stack.getOrDefault(MISSING_PAPER, true);

        if (!missingPaper) {
            textConsumer.accept(Text.translatable("tooltip.hoshikima.fuel")
                    .append(Text.literal(": " + currentFuel + " / " + this.maxFuel))
                    .formatted(Formatting.GRAY));

            textConsumer.accept(Text.translatable("tooltip.hoshikima.power")
                    .append(Text.literal(": " + power))
                    .formatted(Formatting.GRAY));
        } else textConsumer.accept(Text.translatable("tooltip.hoshikima.missing_paper")
                .formatted(Formatting.RED));

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}