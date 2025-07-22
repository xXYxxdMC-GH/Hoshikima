package com.xxyxxdmc.init.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
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

@SuppressWarnings("DuplicateExpressions")
public class MultiFluidBucket extends Item {
    private static final int GRAY = new Color(168, 168, 168).getRGB();

    public MultiFluidBucket(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            int mode = stack.getOrDefault(MODE, 1);
            // Mode 1 is that water, 2 is lava, 3 is powder snow, 4 is spare
            if (mode == 1) {
                stack.set(MODE, 2);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.6F);
                user.sendMessage(Text.translatable("tooltip.hoshikima.chose").append(" ").append(Text.translatable("tooltip.hoshikima.lava").withColor(new Color(221, 76, 0).getRGB())), true);
            } else if (mode == 2) {
                stack.set(MODE, 3);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.8F);
                user.sendMessage(Text.translatable("tooltip.hoshikima.chose").append(" ").append(Text.translatable("tooltip.hoshikima.powder_snow").withColor(new Color(255, 255, 255).getRGB())), true);
            } else if (mode == 3) {
                stack.set(MODE, 4);
                int spare = stack.getOrDefault(SPARE_CAPACITY, 0);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                user.sendMessage(Text.translatable("tooltip.hoshikima.chose")
                        .append(" ")
                        .append(Text.translatable("tooltip.hoshikima.spare"))
                        .append("(")
                        .append(spare == 0 ?
                                Text.translatable("tooltip.hoshikima.empty").formatted(Formatting.GRAY) : spare == 1 ?
                                Text.translatable("tooltip.hoshikima.water").withColor(new Color(0, 116, 216).getRGB()) : spare == 2 ?
                                Text.translatable("tooltip.hoshikima.lava").withColor(new Color(221, 76, 0).getRGB()) :
                                Text.translatable("tooltip.hoshikima.powder_snow").withColor(new Color(255, 255, 255).getRGB()))
                                .append(")")
                        , true);
            } else {
                stack.set(MODE, 1);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.2F);
                user.sendMessage(Text.translatable("tooltip.hoshikima.chose").append(" ").append(Text.translatable("tooltip.hoshikima.water").withColor(new Color(0, 116, 216).getRGB())), true);
            }
            return ActionResult.SUCCESS;
        }

        int mode = stack.getOrDefault(MODE, 1);
        // fillType 0 is empty, 1 is water, 2 is lava, 3 is powder snow, 4 is water and lava, 5 is water and powder snow, 6 is lava and power snow, 7 is all have
        int fillType = stack.getOrDefault(FILL_TYPE, 0);
        // spare 0 is empty, 1 is that water, 2 is lava, 3 is powder snow
        int spare = stack.getOrDefault(SPARE_CAPACITY, 0);

        if (mode == 1) {
            if (fillType == 1 || fillType == 4 || fillType == 5 || fillType == 7) return tryPlaceFluid(world, user, stack);
            else return pickupWater(world, user, stack);
        } else if (mode == 2) {
            if (fillType == 2 || fillType == 4 || fillType == 6 || fillType == 7) return tryPlaceFluid(world, user, stack);
            else return pickupLava(world, user, stack);
        } else if (mode == 3) {
            if (fillType == 3 || fillType == 5 || fillType == 6 || fillType == 7) return placePowderSnow(world, user, stack);
            else return pickupPowderSnow(world, user, stack);
        } else if (mode == 4) {
            if (spare == 0) {
                BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
                if (hitResult.getType() != HitResult.Type.BLOCK) {
                    return ActionResult.PASS;
                }
                BlockPos pos = hitResult.getBlockPos();
                BlockState blockState = world.getBlockState(pos);
                FluidState fluidState = world.getFluidState(pos);
                Fluid fluidToPickup = fluidState.getFluid();
                boolean isWater = fluidToPickup.matchesType(Fluids.WATER);
                boolean isLava = fluidToPickup.matchesType(Fluids.LAVA);

                if (blockState.isOf(Blocks.WATER_CAULDRON) && blockState.get(LeveledCauldronBlock.LEVEL) == 3 || isWater) return pickupWater(world, user, stack);
                else if (blockState.isOf(Blocks.LAVA_CAULDRON) || isLava) return pickupLava(world, user, stack);
                else return pickupPowderSnow(world, user, stack);
            }
            else if (spare == 1 || spare == 2) return tryPlaceFluid(world, user, stack);
            else if (spare == 3) return placePowderSnow(world, user, stack);
        } else return ActionResult.PASS;
        return ActionResult.PASS;
    }

    /**
     * Refresh Multi Bucket State
     * @param fillType 0 is empty, 1 is water, 2 is lava, 3 is powder snow, 4 is water and lava, 5 is water and powder snow, 6 is lava and power snow, 7 is all have
     * @param fluidType 1 is +water, 2 is +lava, 3 is +powder snow, -1 is -water, -2 is -lava, -3 is -powder snow
     * @return Refreshed Multi Bucket State
     */
    private int refreshState(int fillType, int fluidType) {
        if (fluidType >= 0) {
            if (fillType == 0) return fillType + fluidType;
            if (fillType == 1) return fillType * 2 + fluidType;
            if (fillType == 2) return (fluidType == 1) ? 4 : 6;
            if (fillType == 3) return (fluidType == 1) ? 5 : 6;
            if (fillType >= 4) return 7;
        } else {
            if (fillType == 7) return fillType + fluidType;
            if (fillType == 6) return fillType - 1 + fluidType;
            if (fillType == 5) return (fluidType == -1) ? 3 : 1;
            if (fillType == 4) return (fluidType == -1) ? 2 : 1;
            if (fillType <= 3) return 0;
        }
        return 0;
    }

    private ActionResult pickupWater(World world, PlayerEntity player, ItemStack stack) {
        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }
        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        Fluid fluidToPickup = fluidState.getFluid();
        int fillType = stack.getOrDefault(FILL_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        int spare = stack.getOrDefault(SPARE_CAPACITY, 0);

        if (blockState.isOf(Blocks.WATER_CAULDRON) && blockState.get(LeveledCauldronBlock.LEVEL) == 3) {
            if (!world.isClient) {
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                if ((mode == 4 && spare == 0) || ((fillType == 1 || fillType == 4 || fillType == 5 || fillType == 7) && spare == 0)) stack.set(SPARE_CAPACITY, 1);
                else stack.set(FILL_TYPE, refreshState(fillType, 1));
            }
            player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
            return ActionResult.SUCCESS;
        }

        if (!fluidToPickup.matchesType(Fluids.WATER)) return ActionResult.PASS;
        if (blockState.getBlock() instanceof FluidDrainable drainable) {
            ItemStack drainedStack = drainable.tryDrainFluid(player, world, pos, blockState);
            if (!drainedStack.isEmpty()) {
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                if ((mode == 4 && spare == 0) || ((fillType == 1 || fillType == 4 || fillType == 5 || fillType == 7) && spare == 0)) stack.set(SPARE_CAPACITY, 1);
                else stack.set(FILL_TYPE, refreshState(fillType, 1));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private ActionResult pickupLava(World world, PlayerEntity player, ItemStack stack) {
        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }
        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        Fluid fluidToPickup = fluidState.getFluid();
        int fillType = stack.getOrDefault(FILL_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        int spare = stack.getOrDefault(SPARE_CAPACITY, 0);

        if (blockState.isOf(Blocks.LAVA_CAULDRON)) {
            if (!world.isClient) {
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                if ((mode == 4 && spare == 0) || ((fillType == 2 || fillType == 4 || fillType == 6 || fillType == 7) && spare == 0)) stack.set(SPARE_CAPACITY, 2);
                else stack.set(FILL_TYPE, refreshState(fillType, 2));
            }
            player.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
            return ActionResult.SUCCESS;
        }

        if (!fluidToPickup.matchesType(Fluids.LAVA)) return ActionResult.PASS;
        if (blockState.getBlock() instanceof FluidDrainable drainable) {
            ItemStack drainedStack = drainable.tryDrainFluid(player, world, pos, blockState);
            if (!drainedStack.isEmpty()) {
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                player.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                if ((mode == 4 && spare == 0) || ((fillType == 2 || fillType == 4 || fillType == 6 || fillType == 7) && spare == 0)) stack.set(SPARE_CAPACITY, 2);
                else stack.set(FILL_TYPE, refreshState(fillType, 2));
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult pickupPowderSnow(World world, PlayerEntity player, ItemStack stack) {
        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        int fillType = stack.getOrDefault(FILL_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        int spare = stack.getOrDefault(SPARE_CAPACITY, 0);
        if (blockState.isOf(Blocks.POWDER_SNOW_CAULDRON) && blockState.get(LeveledCauldronBlock.LEVEL) == 3) {
            if (!world.isClient) {
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                if ((mode == 4 && spare == 0) || ((fillType == 3 || fillType == 5 || fillType == 6 || fillType == 7) && spare == 0)) stack.set(SPARE_CAPACITY, 3);
                else stack.set(FILL_TYPE, refreshState(fillType, 3));
            }
            player.playSound(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
            return ActionResult.SUCCESS;
        }

        if (blockState.isOf(Blocks.POWDER_SNOW)) {
            world.breakBlock(pos, false);
            player.playSound(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW, 1.0F, 1.0F);
            if (!world.isClient) {
                if ((mode == 4 && spare == 0) || ((fillType == 3 || fillType == 5 || fillType == 6 || fillType == 7) && spare == 0)) stack.set(SPARE_CAPACITY, 3);
                else stack.set(FILL_TYPE, refreshState(fillType, 3));
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private ActionResult placePowderSnow(World world, PlayerEntity player, ItemStack stack) {
        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        int fillType = stack.getOrDefault(FILL_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        int spare = stack.getOrDefault(SPARE_CAPACITY, 0);

        if (blockState.isOf(Blocks.CAULDRON)) {
            if (!world.isClient) {
                world.setBlockState(pos, Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
                player.incrementStat(Stats.FILL_CAULDRON);
                if (mode == 4 && spare == 3) stack.set(SPARE_CAPACITY, 0);
                else if (mode == 3 && spare == 3) {
                    stack.set(SPARE_CAPACITY, 0);
                    player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
                }
                else stack.set(FILL_TYPE, refreshState(fillType, -3));
            }
            player.playSound(SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
            return ActionResult.SUCCESS;
        }

        BlockPos posToPlace = blockState.isReplaceable() ? pos : pos.offset(hitResult.getSide());
        if (world.getBlockState(posToPlace).isReplaceable()) {
            if (!world.isClient) {
                world.setBlockState(posToPlace, Blocks.POWDER_SNOW.getDefaultState());
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    Criteria.PLACED_BLOCK.trigger(serverPlayer, posToPlace, stack);
                }
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                if (mode == 4 && spare == 3) stack.set(SPARE_CAPACITY, 0);
                else if (mode == 3 && spare == 3) {
                    stack.set(SPARE_CAPACITY, 0);
                    player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
                }
                else stack.set(FILL_TYPE, refreshState(fillType, -3));
            }
            player.playSound(SoundEvents.BLOCK_POWDER_SNOW_PLACE, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, posToPlace);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private ActionResult tryPlaceFluid(World world, PlayerEntity player, ItemStack stack) {
        int mode = stack.getOrDefault(MODE, 1);
        int spare = stack.getOrDefault(SPARE_CAPACITY, 0);

        Fluid fluidToPlace = Fluids.EMPTY;
        if (mode == 1) {
            fluidToPlace = Fluids.WATER;
        } else if (mode == 2) {
            fluidToPlace = Fluids.LAVA;
        } else if (mode == 4) {
            if (spare == 1) fluidToPlace = Fluids.WATER;
            else if (spare == 2) fluidToPlace = Fluids.LAVA;
        }

        if (fluidToPlace == Fluids.EMPTY) return ActionResult.PASS;

        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }

        BlockPos posToPlace = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(posToPlace);
        int fillType = stack.getOrDefault(FILL_TYPE, 0);

        if (blockState.isOf(Blocks.CAULDRON)) {
            if (fluidToPlace.matchesType(Fluids.WATER)) {
                if (!world.isClient) {
                    player.incrementStat(Stats.FILL_CAULDRON);
                    world.setBlockState(posToPlace, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
                    world.emitGameEvent(null, GameEvent.FLUID_PLACE, posToPlace);
                    if (mode == 4 && spare == 1) stack.set(SPARE_CAPACITY, 0);
                    else if (mode == 1 && spare == 1) {
                        stack.set(SPARE_CAPACITY, 0);
                        player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1.2F);
                    } else stack.set(FILL_TYPE, refreshState(fillType, -1));
                }
                player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            } else if (fluidToPlace.matchesType(Fluids.LAVA)) {
                if (!world.isClient) {
                    player.incrementStat(Stats.FILL_CAULDRON);
                    world.setBlockState(posToPlace, Blocks.LAVA_CAULDRON.getDefaultState());
                    world.emitGameEvent(null, GameEvent.FLUID_PLACE, posToPlace);
                    if (mode == 4 && spare == 2) stack.set(SPARE_CAPACITY, 0);
                    else if (mode == 2 && spare == 2) {
                        stack.set(SPARE_CAPACITY, 0);
                        player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 0.6F);
                    } else stack.set(FILL_TYPE, refreshState(fillType, -2));
                }
                player.playSound(SoundEvents.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            }
        }

        BlockPos finalPos = blockState.getBlock() instanceof FluidFillable ? posToPlace : posToPlace.offset(hitResult.getSide());

        if (this.placeFluid(player, world, finalPos, fluidToPlace)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                Criteria.PLACED_BLOCK.trigger(serverPlayer, finalPos, stack);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (fluidToPlace.matchesType(Fluids.WATER)) {
                if (mode == 4 && spare == 1) stack.set(SPARE_CAPACITY, 0);
                else if (mode == 1 && spare == 1) {
                    stack.set(SPARE_CAPACITY, 0);
                    player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1.2F);
                } else stack.set(FILL_TYPE, refreshState(fillType, -1));
            } else {
                if (mode == 4 && spare == 2) stack.set(SPARE_CAPACITY, 0);
                else if (mode == 2 && spare == 2) {
                    stack.set(SPARE_CAPACITY, 0);
                    player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 0.6F);
                } else stack.set(FILL_TYPE, refreshState(fillType, -2));
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
            if (world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 11)) {
                playEmptyingSound(player, world, pos, fluid);
                return true;
            }
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
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        int fillType = stack.getOrDefault(FILL_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        int spare = stack.getOrDefault(SPARE_CAPACITY, 0);
        textConsumer.accept(Text.translatable("tooltip.hoshikima.state")
                .append(": ")
                .append(Text.translatable("tooltip.hoshikima.water")
                        .withColor((fillType == 1 || fillType == 4 || fillType == 5 || fillType == 7) ?
                                new Color(0, 116, 216).getRGB() : GRAY))
                .append("/")
                .append(Text.translatable("tooltip.hoshikima.lava")
                        .withColor((fillType == 2 || fillType == 4 || fillType == 6 || fillType == 7) ?
                                new Color(221, 76, 0).getRGB() : GRAY))
                .append("/")
                .append(Text.translatable("tooltip.hoshikima.powder_snow")
                        .withColor((fillType == 3 || fillType == 5 || fillType == 6 || fillType == 7) ?
                                new Color(255, 255, 255).getRGB() : GRAY))
                .append("/")
                .append(Text.translatable("tooltip.hoshikima.spare")
                        .withColor(spare == 0 ?
                                new Color(168, 168, 168).getRGB() : spare == 1 ?
                                new Color(0, 116, 216).getRGB() : spare == 2 ?
                                new Color(221, 76, 0).getRGB() :
                                new Color(255, 255, 255).getRGB())
                )
        );
        textConsumer.accept(Text.translatable("tooltip.hoshikima.mode")
                .append(": ")
                .append(Text.translatable("tooltip.hoshikima." + (mode == 1 ? "water" :
                        (mode == 2 ? "lava" : (mode == 3 ? "powder_snow" : "spare"))))
                        .withColor((mode == 1 ? new Color(0, 116, 216).getRGB() :
                                (mode == 2 ? new Color(221, 76, 0).getRGB() :
                                        (mode == 3 ? new Color(255, 255, 255).getRGB() :
                                                new Color(168, 168, 168).getRGB()))))
                        .append((mode != 4) ? "" : "(")
                        .append((mode != 4) ? Text.empty() : (spare == 0) ?
                                Text.translatable("tooltip.hoshikima.empty").formatted(Formatting.GRAY) : spare == 1 ?
                                Text.translatable("tooltip.hoshikima.water").withColor(new Color(0, 116, 216).getRGB()) : spare == 2 ?
                                Text.translatable("tooltip.hoshikima.lava").withColor(new Color(221, 76, 0).getRGB()) :
                                Text.translatable("tooltip.hoshikima.powder_snow"))
                        .append((mode != 4) ? "" : ")")
                )
        );
    }
}
