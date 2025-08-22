package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.init.api.IChainMineState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class ModMessages {
    private static final HoshikimaConfig config = HoshikimaConfig.get();

    public static void registerC2SPayloads() {
        PayloadTypeRegistry.playC2S().register(ChainMineKeyPressPayload.ID, ChainMineKeyPressPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(QueryChainMineBlocksPacket.ID, QueryChainMineBlocksPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeChainModePayload.ID, ChangeChainModePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ChainMineKeyPressPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            boolean isPressed = payload.isPressed();

            context.server().execute(() -> {
                IChainMineState playerState = (IChainMineState) player;
                playerState.setChainMiningActive(isPressed);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(QueryChainMineBlocksPacket.ID, (payload, context) -> context.server().execute(() -> {
            ServerPlayerEntity player = context.player();
            BlockPos startPos = payload.pos();
            World world = player.getWorld();


            boolean isPressed = payload.isPressed();
            Direction direction = Direction.byIndex(payload.direction());
            IChainMineState playerState = (IChainMineState) player;
            playerState.setChainMiningActive(isPressed);

            if (!isPressed || startPos.equals(new BlockPos(0, 0, 0))) {
                ServerPlayNetworking.send(player, new UpdateChainMineOutlinePacket(Collections.emptyList(), config.chainMode, 0));
                return;
            }

            BlockState startState = world.getBlockState(startPos);

            if (startState.isAir() || (startState.getHardness(world, startPos) < 0 && !player.isCreative())) {
                ServerPlayNetworking.send(player, new UpdateChainMineOutlinePacket(Collections.emptyList(), config.chainMode, 0));
                playerState.setAbleBreak(false);
                return;
            }

            playerState.setAbleBreak(true);

            List<BlockPos> result = new ArrayList<>();

            int airsInTotal = 0;

            switch (config.chainMode) {
                case 0 -> {
                    result = findConnectedBlocks(world, startPos, startState.getBlock());
                    playerState.setTotalSkipAirs(0);
                }
                case 1 -> {
                    if (direction == null) break;
                    Direction face = direction.getOpposite();

                    int dx = face.getOffsetX();
                    int dy = face.getOffsetY();
                    int dz = face.getOffsetZ();
                    int airs = 0;
                    int totalAirs = 0;
                    for (int i = 0; i < config.blockChainLimit; i++) {
                        BlockPos target = startPos.add(dx * i, dy * i, dz * i);
                        BlockState state = world.getBlockState(target);
                        if (state.isAir()) {
                            if (airs < config.skipAirBlocksInOnce && totalAirs < config.skipAirBlocksInTotal) {
                                airs++;
                                totalAirs++;
                                continue;
                            } else {
                                totalAirs-=airs;
                                break;
                            }
                        } else airs = 0;
                        if (state.getHardness(world, target) < 0 && !player.isCreative()) break;
                        result.add(target);
                    }
                    airsInTotal = totalAirs;
                    playerState.setTotalSkipAirs(totalAirs);
                }
                case 2 -> {
                    if (direction == null) break;

                    Direction face = direction.getOpposite();

                    int mainDx = face.getOffsetX();
                    int mainDy = face.getOffsetY();
                    int mainDz = face.getOffsetZ();

                    List<BlockPos> offsets = new ArrayList<>();

                    if (face.getAxis().isVertical()) {
                        Direction perpendicularDir = player.getHorizontalFacing().rotateYClockwise();
                        int perDx = perpendicularDir.getOffsetX();
                        int perDz = perpendicularDir.getOffsetZ();

                        offsets.add(new BlockPos(0, 0, 0));
                        offsets.add(new BlockPos(perDx, 0, perDz));
                    } else {
                        offsets.add(new BlockPos(0, 0, 0));
                        offsets.add(new BlockPos(0, 1, 0));
                    }

                    int airs = 0;
                    int totalAirs = 0;
                    boolean shouldBreak = false;
                    int distance = 0;

                    while (result.size() < config.blockChainLimit) {
                        BlockPos currentPos = startPos.add(mainDx * distance, mainDy * distance, mainDz * distance);

                        List<BlockPos> currentStepBlocks = new ArrayList<>();

                        for (BlockPos offset : offsets) {
                            BlockPos target = currentPos.add(offset);
                            BlockState state = world.getBlockState(target);

                            if (result.size() >= config.blockChainLimit) {
                                shouldBreak = true;
                                break;
                            }

                            if (state.isAir()) {
                                if (airs < config.skipAirBlocksInOnce * offsets.size() && totalAirs < config.skipAirBlocksInTotal * offsets.size()) {
                                    airs++;
                                    totalAirs++;
                                } else {
                                    shouldBreak = true;
                                    break;
                                }
                            } else {
                                airs = 0;
                                if (state.getHardness(world, target) < 0 && !player.isCreative()) {
                                    shouldBreak = true;
                                    break;
                                }
                                currentStepBlocks.add(target);
                            }
                        }

                        if (shouldBreak) {
                            break;
                        }

                        for (BlockPos block : currentStepBlocks) {
                            if (result.size() < config.blockChainLimit) {
                                result.add(block);
                            } else {
                                break;
                            }
                        }

                        distance++;
                    }
                    airsInTotal = totalAirs;
                    playerState.setTotalSkipAirs(totalAirs);
                }
                case 3 -> {
                    if (direction == null) break;

                    Direction face = direction.getOpposite();

                    int mainDx = face.getOffsetX();
                    int mainDy = face.getOffsetY();
                    int mainDz = face.getOffsetZ();

                    List<BlockPos> offsets = new ArrayList<>();

                    if (face.getAxis().isVertical()) {
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                offsets.add(new BlockPos(x, 0, z));
                            }
                        }
                    } else {
                        Direction perpendicularDir = player.getHorizontalFacing().rotateYClockwise();
                        int perpDx = perpendicularDir.getOffsetX();
                        int perpDz = perpendicularDir.getOffsetZ();

                        for (int y = -1; y <= 1; y++) {
                            for (int p = -1; p <= 1; p++) {
                                offsets.add(new BlockPos(perpDx * p, y, perpDz * p));
                            }
                        }
                    }

                    int airs = 0;
                    int totalAirs = 0;
                    boolean shouldBreak = false;
                    int distance = 0;

                    while (result.size() < config.blockChainLimit) {
                        BlockPos currentPos = startPos.add(mainDx * distance, mainDy * distance, mainDz * distance);

                        List<BlockPos> currentStepBlocks = new ArrayList<>();

                        for (BlockPos offset : offsets) {
                            BlockPos target = currentPos.add(offset);
                            BlockState state = world.getBlockState(target);

                            if (result.size() >= config.blockChainLimit) {
                                shouldBreak = true;
                                break;
                            }

                            if (state.isAir()) {
                                if (airs < config.skipAirBlocksInOnce * offsets.size() && totalAirs < config.skipAirBlocksInTotal * offsets.size()) {
                                    airs++;
                                    totalAirs++;
                                } else {
                                    shouldBreak = true;
                                    totalAirs-=airs;
                                    break;
                                }
                            } else {
                                airs = 0;
                                if (state.getHardness(world, target) < 0 && !player.isCreative()) {
                                    shouldBreak = true;
                                    break;
                                }
                                currentStepBlocks.add(target);
                            }
                        }

                        if (shouldBreak) {
                            break;
                        }

                        for (BlockPos block : currentStepBlocks) {
                            if (result.size() < config.blockChainLimit) {
                                result.add(block);
                            } else {
                                break;
                            }
                        }

                        distance++;
                    }
                    airsInTotal = totalAirs;
                    playerState.setTotalSkipAirs(totalAirs);
                }
                default -> {
                    result = Collections.emptyList();
                    playerState.setTotalSkipAirs(0);
                }
            }

            playerState.setPendingBreakList(result);

            ServerPlayNetworking.send(
                player,
                new UpdateChainMineOutlinePacket(
                    result,
                    config.chainMode,
                    airsInTotal
                )
            );
        }));

        ServerPlayNetworking.registerGlobalReceiver(ChangeChainModePayload.ID, (payload, context) -> {
            boolean isUp = payload.value();

            context.server().execute(() -> {
                int currentMode = config.chainMode;
                if (isUp) currentMode++;
                else currentMode--;
                if (currentMode > 3) currentMode = 0;
                else if (currentMode < 0) currentMode = 3;
                config.chainMode = currentMode;

                ServerPlayNetworking.send(context.player(), new UpdateChainModePayload(currentMode));
            });
        });
    }

    public static void registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(UpdateChainMineOutlinePacket.ID, UpdateChainMineOutlinePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateChainModePayload.ID, UpdateChainModePayload.CODEC);
    }

    private static List<BlockPos> findConnectedBlocks(World world, BlockPos startPos, Block originalBlock) {
        List<BlockPos> foundBlocks = new ArrayList<>();
        Queue<BlockPos> blocksToVisit = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        blocksToVisit.add(startPos);
        visited.add(startPos);

        while (!blocksToVisit.isEmpty() && foundBlocks.size() < config.blockChainLimit) {
            BlockPos currentPos = blocksToVisit.poll();

            if (world.getBlockState(currentPos).isOf(originalBlock)) {
                if (foundBlocks.size() < config.blockChainLimit) {
                    foundBlocks.add(currentPos);
                    addNeighborsToQueue(currentPos, blocksToVisit, visited);
                }
            }
        }
        return foundBlocks;
    }

    private static void addNeighborsToQueue(BlockPos pos, Queue<BlockPos> queue, Set<BlockPos> visited) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos neighborPos = pos.add(x, y, z);
                    if (visited.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                }
            }
        }
    }
}
