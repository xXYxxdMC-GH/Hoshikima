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
                            } else break;
                        } else airs = 0;
                        if (state.getHardness(world, target) < 0 && !player.isCreative()) break;
                        result.add(target);
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
    }

    public static void registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(UpdateChainMineOutlinePacket.ID, UpdateChainMineOutlinePacket.CODEC);
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
