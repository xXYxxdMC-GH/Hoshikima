package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.init.callback.IChainMineState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
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
            Direction direction = Direction.byIndex(payload.direction());

            context.server().execute(() -> {
                IChainMineState playerState = (IChainMineState) player;
                playerState.setChainMiningActive(isPressed);
                playerState.setDirection(direction);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(QueryChainMineBlocksPacket.ID, (payload, context) -> {
    context.server().execute(() -> {
        ServerPlayerEntity player = context.player();
        BlockPos startPos = payload.pos();
        World world = player.getWorld();
        BlockState startState = world.getBlockState(startPos);

        if (startState.isAir() || startState.getHardness(world, startPos) < 0) {
            ServerPlayNetworking.send(player, new UpdateChainMineOutlinePacket(Collections.emptyList()));
            return;
        }

        List<BlockPos> result = new ArrayList<>();
        IChainMineState playerState = (IChainMineState) player;

        switch (config.chainMode) {
            case 0 -> {
                result = findConnectedBlocks(world, startPos, startState.getBlock());
                result.add(0, startPos);
                break;
            }
            case 1 -> {
                Direction face = playerState.getDirection();
                if (face == null) break;

                Direction direction = face.getOpposite();
                int dx = direction.getOffsetX();
                int dy = direction.getOffsetY();
                int dz = direction.getOffsetZ();

                for (int i = 0; i < config.blockChainLimit; i++) {
                    BlockPos target = startPos.add(dx * i, dy * i, dz * i);
                    BlockState state = world.getBlockState(target);
                    if (state.isAir()) continue;
                    if (state.getHardness(world, target) < 0) break;
                    result.add(target);
                }
                break;
            }
            default -> {
                result = Collections.emptyList();
                break;
            }
        }

        playerState.setPendingBreakList(result);

        ServerPlayNetworking.send(player, new UpdateChainMineOutlinePacket(result));
    });
});
}

    public static void registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(UpdateChainMineOutlinePacket.ID, UpdateChainMineOutlinePacket.CODEC);
    }

    private static List<BlockPos> findConnectedBlocks(World world, BlockPos startPos, Block originalBlock) {
        List<BlockPos> foundBlocks = new ArrayList<>();
        Queue<BlockPos> blocksToVisit = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        addNeighborsToQueue(startPos, blocksToVisit, visited);
        visited.add(startPos);

        while (!blocksToVisit.isEmpty() && foundBlocks.size() < config.blockChainLimit) {
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

    private static Direction getTargetedFace(World world, ServerPlayerEntity player) {
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0F);
        double reach = player.getAttributes().getValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        Vec3d reachEnd = eyePos.add(lookVec.multiply(reach));

        RaycastContext context = new RaycastContext(
            eyePos,
            reachEnd,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );

        HitResult result = world.raycast(context);
        if (result instanceof BlockHitResult blockHit) {
            return blockHit.getSide();
        }

        return null;
    }
}
