package com.xxyxxdmc.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;

public class ChainMineOutlineRenderer {

    private static List<BlockPos> blocksToRender = Collections.emptyList();

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
                Box box = new Box(pos).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                drawBoxLines(matrixStack, vertexConsumer, box, 1.0f, 0.8f, 0.0f, 1.0f);
            }
        });
    }

    private static void drawBoxLines(MatrixStack matrixStack, VertexConsumer vertexConsumer, Box box, float r, float g, float b, float a) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        vertexConsumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);

        // Top face
        vertexConsumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);

        // Vertical lines
        vertexConsumer.vertex(matrix, minX, minY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);

        vertexConsumer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);
        vertexConsumer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a).normal(0f, 1f, 0f);
    }

    public static void setBlocksToRender(List<BlockPos> blocks) {
        MinecraftClient.getInstance().execute(() -> blocksToRender = blocks);
    }

    public static void clear() {
        if (!blocksToRender.isEmpty()) {
            MinecraftClient.getInstance().execute(() -> blocksToRender = Collections.emptyList());
        }
    }
}