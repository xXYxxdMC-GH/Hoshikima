package com.xxyxxdmc.init.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.*;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Consumer;

import static com.xxyxxdmc.init.ModDataComponents.*;

public class LargeBucket extends Item {
    private final int maxCapacity = 8;

    public LargeBucket(Settings settings) {
        super(settings.maxCount(1).component(FLUID_TYPE, 0).component(WATER_CAPACITY, 0).component(LAVA_CAPACITY, 0).component(MODE, 1));
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.getOrDefault(WATER_CAPACITY, 0) > 0 || stack.getOrDefault(LAVA_CAPACITY, 0) > 0;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            int mode = stack.getOrDefault(MODE, 1);
            if (mode == 1) {
                stack.set(MODE, 2);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.2F);
            } else {
                stack.set(MODE, 1);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.8F);
            }
            return ActionResult.SUCCESS;
        }

        int waterCount = stack.getOrDefault(WATER_CAPACITY, 0);
        int lavaCount = stack.getOrDefault(LAVA_CAPACITY, 0);
        int mode = stack.getOrDefault(MODE, 1);

        if (mode == 1 && waterCount < maxCapacity && lavaCount < maxCapacity) {
            return tryPickupFluid(world, user, stack);
        } else if (mode == 2) {
            return tryPlaceFluid(world, user, stack);
        } else return ActionResult.PASS;
    }

    private ActionResult tryPickupFluid(World world, PlayerEntity player, ItemStack stack) {
        int waterCount = stack.getOrDefault(WATER_CAPACITY, 0);
        int lavaCount = stack.getOrDefault(LAVA_CAPACITY, 0);

        if (waterCount >= maxCapacity || lavaCount >= maxCapacity) {
            return ActionResult.PASS;
        }

        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        Fluid fluidToPickup = fluidState.getFluid();

        boolean isWater = fluidToPickup.matchesType(Fluids.WATER);
        boolean isLava = fluidToPickup.matchesType(Fluids.LAVA);
        if (!isWater && !isLava) {
            return ActionResult.PASS;
        }

        if ((isWater && lavaCount > 0) || (isLava && waterCount > 0)) {
            return ActionResult.PASS;
        }

        if (blockState.getBlock() instanceof FluidDrainable drainable) {
            ItemStack drainedStack = drainable.tryDrainFluid(player, world, pos, blockState);
            if (!drainedStack.isEmpty()) {
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                SoundEvent soundEvent = fluidToPickup.getBucketFillSound().orElse(SoundEvents.ITEM_BUCKET_FILL);
                player.playSound(soundEvent, 1.0F, 1.0F);
                world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);

                if (isWater) {
                    stack.set(WATER_CAPACITY, waterCount + 1);
                    stack.set(FLUID_TYPE, 1);
                } else {
                    stack.set(LAVA_CAPACITY, lavaCount + 1);
                    stack.set(FLUID_TYPE, 2);
                }
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult tryPlaceFluid(World world, PlayerEntity player, ItemStack stack) {
        int waterCount = stack.getOrDefault(WATER_CAPACITY, 0);
        int lavaCount = stack.getOrDefault(LAVA_CAPACITY, 0);

        Fluid fluidToPlace = Fluids.EMPTY;
        if (waterCount > 0) {
            fluidToPlace = Fluids.WATER;
        } else if (lavaCount > 0) {
            fluidToPlace = Fluids.LAVA;
        }

        if (fluidToPlace == Fluids.EMPTY) return ActionResult.PASS;

        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }

        BlockPos posToPlace = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(posToPlace);

        BlockPos finalPos = blockState.getBlock() instanceof FluidFillable ? posToPlace : posToPlace.offset(hitResult.getSide());

        if (this.placeFluid(player, world, finalPos, fluidToPlace)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Criteria.PLACED_BLOCK.trigger(serverPlayer, finalPos, stack);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (fluidToPlace.matchesType(Fluids.WATER)) {
                stack.set(WATER_CAPACITY, waterCount - 1);
            } else {
                stack.set(LAVA_CAPACITY, lavaCount - 1);
            }

            if (waterCount - 1 == 0 || lavaCount - 1 == 0) {
                stack.set(FLUID_TYPE, 0);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.FAIL;
    }

    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, Fluid fluid) {
        if (!(fluid instanceof FlowableFluid flowableFluid)) {
            return false;
        }

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (block == fluid.getDefaultState().getBlockState().getBlock()) {
            playEmptyingSound(player, world, pos, fluid);
            return true;
        }

        if (block instanceof FluidFillable fluidFillable && fluidFillable.canFillWithFluid(player, world, pos, blockState, fluid)) {
            fluidFillable.tryFillWithFluid(world, pos, blockState, flowableFluid.getStill(false));
            playEmptyingSound(player, world, pos, fluid);
            return true;
        }

        if (!blockState.canBucketPlace(fluid)) {
            return false;
        }

        if (world.getDimension().ultrawarm() && fluid.isIn(FluidTags.WATER)) {
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                world.addImportantParticleClient(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }

        if (!world.isClient && blockState.isReplaceable() && !blockState.isLiquid()) {
            world.breakBlock(pos, true);
        }

        if (world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 11)) {
            playEmptyingSound(player, world, pos, fluid);
            return true;
        }

        return false;
    }

    protected void playEmptyingSound(@Nullable PlayerEntity user, WorldAccess world, BlockPos pos, Fluid fluid) {
        world.playSound(user, pos, fluid.isIn(FluidTags.WATER) ? SoundEvents.ITEM_BUCKET_EMPTY : SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(user, GameEvent.FLUID_PLACE, pos);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int type = stack.getOrDefault(FLUID_TYPE, 1);
        return switch (type) {
            case 1 -> Math.round(13.0F * stack.getOrDefault(WATER_CAPACITY, 0) / maxCapacity);
            case 2 -> Math.round(13.0F * stack.getOrDefault(LAVA_CAPACITY, 0) / maxCapacity);
            default -> 0;
        };
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int type = stack.getOrDefault(FLUID_TYPE, 1);
        return switch (type) {
            case 1 -> new Color(0, 116, 216).getRGB();
            case 2 -> new Color(221, 76, 0).getRGB();
            default -> 0x000000;
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        int currentFluid = stack.getOrDefault(FLUID_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        if (mode == 1) textConsumer.accept(Text.translatable("tooltip.randomthing.mode").append(": ").append(Text.translatable("tooltip.randomthing.load")));
        else textConsumer.accept(Text.translatable("tooltip.randomthing.mode").append(": ").append(Text.translatable("tooltip.randomthing.unload")));
        if (currentFluid != 0) {
            int currentCapacity = stack.getOrDefault(currentFluid == 1 ? WATER_CAPACITY : LAVA_CAPACITY, 0);
            if (currentFluid == 1) textConsumer.accept(Text.translatable("tooltip.randomthing.water").withColor(new Color(0, 116, 216).getRGB()));
            else textConsumer.accept(Text.translatable("tooltip.randomthing.lava").withColor(new Color(221, 76, 0).getRGB()));
            textConsumer.accept(Text.translatable("tooltip.randomthing.capacity")
                    .append(Text.literal(": " + currentCapacity + " / " + maxCapacity))
                    .formatted(Formatting.GRAY));
        } else textConsumer.accept(Text.translatable("tooltip.randomthing.empty")
                .formatted(Formatting.GRAY));
    }
}
