package com.xxyxxdmc.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChainMineOutlineRenderer {

    private static Set<BlockPos> blocksToRender = Collections.emptySet();
    private static final float R = 1.0f, G = 0.8f, B = 0.0f, A = 1.0f;

    public static void init() {
        WorldRenderEvents.LAST.register(context -> {
            if (blocksToRender.isEmpty()) {
                return;
            }

            VertexConsumerProvider vertexConsumerProvider = context.consumers();
            if (vertexConsumerProvider == null) {
                return;
            }
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());

            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();
            MatrixStack matrixStack = context.matrixStack();

            for (BlockPos pos : blocksToRender) {
                matrixStack.push();
                matrixStack.translate(
                        pos.getX() - cameraPos.getX(),
                        pos.getY() - cameraPos.getY(),
                        pos.getZ() - cameraPos.getZ()
                );

                Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

                boolean hasNeighborN = blocksToRender.contains(pos.north());
                boolean hasNeighborS = blocksToRender.contains(pos.south());
                boolean hasNeighborE = blocksToRender.contains(pos.east());
                boolean hasNeighborW = blocksToRender.contains(pos.west());
                boolean hasNeighborU = blocksToRender.contains(pos.up());
                boolean hasNeighborD = blocksToRender.contains(pos.down());

                float x0 = 0f, y0 = 0f, z0 = 0f;
                float x1 = 1f, y1 = 1f, z1 = 1f;

                if (!hasNeighborW || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x0, y0, z0, x0, y1, z0);
                if (!hasNeighborE || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x1, y0, z0, x1, y1, z0);
                if (!hasNeighborW || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x0, y0, z1, x0, y1, z1);
                if (!hasNeighborE || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x1, y0, z1, x1, y1, z1);

                if (!hasNeighborU || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x0, y1, z0, x1, y1, z0);
                if (!hasNeighborU || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x0, y1, z1, x1, y1, z1);
                if (!hasNeighborD || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x0, y0, z0, x1, y0, z0);
                if (!hasNeighborD || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x0, y0, z1, x1, y0, z1);

                if (!hasNeighborU || !hasNeighborW) drawLine(positionMatrix, vertexConsumer, x0, y1, z0, x0, y1, z1);
                if (!hasNeighborU || !hasNeighborE) drawLine(positionMatrix, vertexConsumer, x1, y1, z0, x1, y1, z1);
                if (!hasNeighborD || !hasNeighborW) drawLine(positionMatrix, vertexConsumer, x0, y0, z0, x0, y0, z1);
                if (!hasNeighborD || !hasNeighborE) drawLine(positionMatrix, vertexConsumer, x1, y0, z0, x1, y0, z1);

                matrixStack.pop();
            }
        });
    }

    private static void drawLine(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2) {
        consumer.vertex(matrix, x1, y1, z1).color(R, G, B, A).normal(0f, 1f, 0f);
        consumer.vertex(matrix, x2, y2, z2).color(R, G, B, A).normal(0f, 1f, 0f);
    }

    public static void setBlocksToRender(List<BlockPos> blocks) {
        MinecraftClient.getInstance().execute(() -> {
            if (blocks == null || blocks.isEmpty()) {
                blocksToRender = Collections.emptySet();
            } else {
                blocksToRender = new HashSet<>(blocks);
            }
        });
    }

    public static void clear() {
        if (!blocksToRender.isEmpty()) {
            MinecraftClient.getInstance().execute(() -> blocksToRender = Collections.emptySet());
        }
    }
}