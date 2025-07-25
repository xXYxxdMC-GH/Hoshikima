package com.xxyxxdmc.init.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.xxyxxdmc.init.ModDataComponents.*;

public class LargeBucket extends Item {
    private final int maxCapacity = 8;

    public LargeBucket(Settings settings) {
        super(settings.maxCount(1).component(FLUID_TYPE, 0).component(WATER_CAPACITY, 0).component(LAVA_CAPACITY, 0).component(SNOW_CAPACITY, 0).component(MODE, 1).component(ENTITIES_IN_BUCKET, new ArrayList<>()));
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.getOrDefault(FLUID_TYPE, 0) != 0;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            int mode = stack.getOrDefault(MODE, 1);
            if (mode == 1) {
                stack.set(MODE, 2);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.2F);
                user.sendMessage(Text.translatable("tooltip.hoshikima.mode").append(": ").append(Text.translatable("tooltip.hoshikima.unload")), true);
            } else {
                stack.set(MODE, 1);
                user.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.8F);
                user.sendMessage(Text.translatable("tooltip.hoshikima.mode").append(": ").append(Text.translatable("tooltip.hoshikima.load")), true);
            }
            return ActionResult.SUCCESS;
        }

        int mode = stack.getOrDefault(MODE, 1);
        int fluidType = stack.getOrDefault(FLUID_TYPE, 0);

        if (mode == 1) {
            return this.tryPickupFluid(world, user, stack);
        } else if (mode == 2) {
            if (fluidType == 1 || fluidType == 2) {
                return this.tryPlaceFluid(world, user, stack);
            } else if (fluidType == 3) {
                return this.tryPlacePowderSnow(world, user, stack);
            }
        } else return ActionResult.PASS;
        return ActionResult.PASS;
    }

    private ActionResult tryPickupFluid(World world, PlayerEntity player, ItemStack stack) {
        int waterCount = stack.getOrDefault(WATER_CAPACITY, 0);
        int lavaCount = stack.getOrDefault(LAVA_CAPACITY, 0);
        int snowCount = stack.getOrDefault(SNOW_CAPACITY, 0);
        int fluidType = stack.getOrDefault(FLUID_TYPE, 0);

        if (waterCount >= maxCapacity || lavaCount >= maxCapacity || snowCount >= maxCapacity) {
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

        if (blockState.isOf(Blocks.POWDER_SNOW_CAULDRON) && blockState.get(LeveledCauldronBlock.LEVEL) == 3) {
            if (fluidType == 0 || fluidType == 3) {
                if (!world.isClient) {
                    player.incrementStat(Stats.USED.getOrCreateStat(this));
                    world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                    stack.set(SNOW_CAPACITY, snowCount + 1);
                    stack.set(FLUID_TYPE, 3);
                }
                player.playSound(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW, 1.0F, 1.0F);
                world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                return ActionResult.SUCCESS;
            }
        }

        if (blockState.isOf(Blocks.WATER_CAULDRON) && blockState.get(LeveledCauldronBlock.LEVEL) == 3) {
            if (fluidType == 0 || fluidType == 1) {
                if (!world.isClient) {
                    player.incrementStat(Stats.USED.getOrCreateStat(this));
                    world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                    stack.set(WATER_CAPACITY, waterCount + 1);
                    stack.set(FLUID_TYPE, 1);
                }
                player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                return ActionResult.SUCCESS;
            }
        }

        if (blockState.isOf(Blocks.LAVA_CAULDRON)) {
            if (fluidType == 0 || fluidType == 2) {
                if (!world.isClient) {
                    player.incrementStat(Stats.USED.getOrCreateStat(this));
                    world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                    stack.set(LAVA_CAPACITY, lavaCount + 1);
                    stack.set(FLUID_TYPE, 2);
                }
                player.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                return ActionResult.SUCCESS;
            }
        }

        if (blockState.isOf(Blocks.POWDER_SNOW) && (fluidType == 0 || fluidType == 3)) {
            world.breakBlock(pos, false);
            player.playSound(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW, 1.0F, 1.0F);
            if (!world.isClient) {
                stack.set(SNOW_CAPACITY, snowCount + 1);
                stack.set(FLUID_TYPE, 3);
            }
            return ActionResult.SUCCESS;
        }

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

    private ActionResult tryPlacePowderSnow(World world, PlayerEntity player, ItemStack stack) {
        int snowCount = stack.getOrDefault(SNOW_CAPACITY, 0);
        if (snowCount <= 0) {
            return ActionResult.PASS;
        }

        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(pos);

        if (blockState.isOf(Blocks.CAULDRON)) {
            if (!world.isClient) {
                world.setBlockState(pos, Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
                player.incrementStat(Stats.FILL_CAULDRON);
                stack.set(SNOW_CAPACITY, snowCount - 1);
                if (snowCount - 1 == 0) {
                    stack.set(FLUID_TYPE, 0);
                }
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
                stack.set(SNOW_CAPACITY, snowCount - 1);
                if (snowCount - 1 == 0) {
                    stack.set(FLUID_TYPE, 0);
                }
            }
            player.playSound(SoundEvents.BLOCK_POWDER_SNOW_PLACE, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, posToPlace);
            return ActionResult.SUCCESS;
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

        if (blockState.isOf(Blocks.CAULDRON)) {
            if (fluidToPlace.matchesType(Fluids.WATER)) {
                if (!world.isClient) {
                    player.incrementStat(Stats.FILL_CAULDRON);
                    world.setBlockState(posToPlace, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
                    world.emitGameEvent(null, GameEvent.FLUID_PLACE, posToPlace);
                    stack.set(WATER_CAPACITY, waterCount - 1);
                    if (waterCount - 1 == 0) stack.set(FLUID_TYPE, 0);
                }
                player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            } else if (fluidToPlace.matchesType(Fluids.LAVA)) {
                if (!world.isClient) {
                    player.incrementStat(Stats.FILL_CAULDRON);
                    world.setBlockState(posToPlace, Blocks.LAVA_CAULDRON.getDefaultState());
                    world.emitGameEvent(null, GameEvent.FLUID_PLACE, posToPlace);
                    stack.set(LAVA_CAPACITY, lavaCount - 1);
                    if (lavaCount - 1 == 0) stack.set(FLUID_TYPE, 0);
                }
                player.playSound(SoundEvents.ITEM_BUCKET_EMPTY_LAVA, 1.0F, 1.0F);
                return ActionResult.SUCCESS;
            }
        }

        BlockPos finalPos = blockState.getBlock() instanceof FluidFillable ? posToPlace : posToPlace.offset(hitResult.getSide());

        if (this.placeFluid(player, world, finalPos, fluidToPlace, stack)) {
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

    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, Fluid fluid, ItemStack stack) {
        if (!(fluid instanceof FlowableFluid flowableFluid)) {
            return false;
        }

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        int size = stack.getOrDefault(ENTITIES_SIZE, 0);

        if (block == fluid.getDefaultState().getBlockState().getBlock()) {
            if (size > 0) {
                spawnEntity(world, stack, pos);
            }
            playEmptyingSound(player, world, pos, fluid);
            return true;
        }

        if (block == fluid.getDefaultState().getBlockState().getBlock()) {
            if (world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 11)) {
                playEmptyingSound(player, world, pos, fluid);
                return true;
            }
        }

        if (block instanceof FluidFillable fluidFillable && fluidFillable.canFillWithFluid(player, world, pos, blockState, fluid) && size <= 0) {
            fluidFillable.tryFillWithFluid(world, pos, blockState, flowableFluid.getStill(false));
            playEmptyingSound(player, world, pos, fluid);
            return true;
        }

        if (!blockState.canBucketPlace(fluid)) {
            return false;
        }

        if (world.getDimension().ultrawarm() && fluid.isIn(FluidTags.WATER)) {
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            if (size > 0) spawnEntity(world, stack, pos);
            for (int l = 0; l < 8; ++l) {
                world.addImportantParticleClient(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }

        if (!world.isClient && blockState.isReplaceable() && !blockState.isLiquid()) {
            world.breakBlock(pos, true);
        }

        if (world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 11)) {
            if (size > 0) spawnEntity(world, stack, pos);
            playEmptyingSound(player, world, pos, fluid);
            return true;
        }

        return false;
    }

    private void spawnEntity(World world, ItemStack stack, BlockPos pos) {
        List<NbtCompound> originalEntities = stack.getOrDefault(ENTITIES_IN_BUCKET, List.of());
        if (originalEntities.isEmpty()) {
            return;
        }
        NbtCompound nbtCompound = originalEntities.getLast();
        String entity = originalEntities.getLast().getString("id", "minecraft:cod");
        EntityType<? extends MobEntity> entityType = switch (entity) {
          case "minecraft:axolotl" -> EntityType.AXOLOTL;
          case "minecraft:salmon" -> EntityType.SALMON;
          case "minecraft:tropical_fish" -> EntityType.TROPICAL_FISH;
          case "minecraft:pufferfish" -> EntityType.PUFFERFISH;
          case "minecraft:tadpole" -> EntityType.TADPOLE;
          default -> EntityType.COD;
        };
        MobEntity mobEntity = entityType.create(world, SpawnReason.BUCKET);
        if (mobEntity instanceof Bucketable bucketable) {
            if (nbtCompound.contains("BucketTicks")) nbtCompound.remove("BucketTicks");
            mobEntity.readNbt(nbtCompound);

            List<NbtCompound> updatedEntities = new ArrayList<>();
            for (int i = 0; i < originalEntities.size() - 1; i++) {
                updatedEntities.add(originalEntities.get(i).copy());
            }
            stack.set(ENTITIES_IN_BUCKET, updatedEntities);
            stack.set(ENTITIES_SIZE, stack.getOrDefault(ENTITIES_SIZE, 0) - 1);
            bucketable.setFromBucket(true);
        }
        if (mobEntity != null) {
            world.spawnEntity(mobEntity);
            mobEntity.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, (float) (Math.random() * 180.0F), (float) (Math.random() * 30.0F));
            mobEntity.playAmbientSound();
        }
    }

    protected void playEmptyingSound(@Nullable PlayerEntity user, WorldAccess world, BlockPos pos, Fluid fluid) {
        world.playSound(user, pos, fluid.isIn(FluidTags.WATER) ? SoundEvents.ITEM_BUCKET_EMPTY : SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(user, GameEvent.FLUID_PLACE, pos);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (stack.getOrDefault(ENTITIES_SIZE, 0) <= 0) return;
        if (!entity.isPlayer() || Objects.requireNonNull(entity.getServer()).getTicks() % 2 != 0) {
        }

    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int type = stack.getOrDefault(FLUID_TYPE, 1);
        return switch (type) {
            case 1 -> Math.round(13.0F * stack.getOrDefault(WATER_CAPACITY, 0) / maxCapacity);
            case 2 -> Math.round(13.0F * stack.getOrDefault(LAVA_CAPACITY, 0) / maxCapacity);
            case 3 -> Math.round(13.0F * stack.getOrDefault(SNOW_CAPACITY, 0) / maxCapacity);
            default -> 0;
        };
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int type = stack.getOrDefault(FLUID_TYPE, 1);
        return switch (type) {
            case 1 -> new Color(0, 116, 216).getRGB();
            case 2 -> new Color(221, 76, 0).getRGB();
            case 3 -> new Color(255,255,255).getRGB();
            default -> 0x000000;
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}
