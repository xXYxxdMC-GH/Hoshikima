package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.init.callback.IChainMineState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class ModMessages {
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


        ServerPlayNetworking.registerGlobalReceiver(QueryChainMineBlocksPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                BlockPos startPos = payload.pos();
                net.minecraft.world.World world = context.player().getWorld();
                BlockState startState = world.getBlockState(startPos);

                if (startState.isAir() || startState.getHardness(world, startPos) < 0) {
                    ServerPlayNetworking.send(context.player(), new UpdateChainMineOutlinePacket(Collections.emptyList()));
                    return;
                }

                boolean isOre = startState.isIn(BlockTags.DIAMOND_ORES);
                boolean isLog = startState.isIn(BlockTags.LOGS);
                boolean isConfigured = HoshikimaConfig.get().chainableBlocks.contains(startState.getBlock());

                if (!isOre && !isLog && !isConfigured) {
                    ServerPlayNetworking.send(context.player(), new UpdateChainMineOutlinePacket(Collections.emptyList()));
                    return;
                }

                List<BlockPos> connectedBlocks = findConnectedBlocks(world, startPos, startState.getBlock());
                connectedBlocks.addFirst(startPos);

                ServerPlayNetworking.send(context.player(), new UpdateChainMineOutlinePacket(connectedBlocks));
            });
        });
    }

    public static void registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(UpdateChainMineOutlinePacket.ID, UpdateChainMineOutlinePacket.CODEC);
    }

    private static List<BlockPos> findConnectedBlocks(net.minecraft.world.World world, BlockPos startPos, net.minecraft.block.Block originalBlock) {
        List<BlockPos> foundBlocks = new ArrayList<>();
        Queue<BlockPos> blocksToVisit = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        addNeighborsToQueue(startPos, blocksToVisit, visited);
        visited.add(startPos);

        while (!blocksToVisit.isEmpty() && foundBlocks.size() < 128) {
            BlockPos currentPos = blocksToVisit.poll();

            if (world.getBlockState(currentPos).isOf(originalBlock)) {
                foundBlocks.add(currentPos);
                addNeighborsToQueue(currentPos, blocksToVisit, visited);
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
