package com.xxyxxdmc.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.*;

public class ChainMineOutlineRenderer {

    private static Set<BlockPos> blocksToRender = Collections.emptySet();
    // 恢复你喜欢的黄色
    private static final float R = 1.0f, G = 0.8f, B = 0.0f, A = 1.0f;

    // 我们之前创建的自定义RenderLayer，确保线条总是可见
    private static final RenderLayer LINES_NO_DEPTH = RenderLayer.of("hoshikima_lines_no_depth",
            VertexFormats.POSITION_COLOR_NORMAL,
            VertexFormat.DrawMode.LINES,
            256,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2.0f)))
                    .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
                    .depthTest(RenderLayer.ALWAYS_DEPTH_TEST)
                    .cull(RenderLayer.DISABLE_CULLING)
                    .build(false));

    public static void init() {
        WorldRenderEvents.LAST.register(context -> {
            if (blocksToRender.isEmpty()) {
                return;
            }

            VertexConsumerProvider vertexConsumerProvider = context.consumers();
            if (vertexConsumerProvider == null) {
                return;
            }
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LINES_NO_DEPTH);

            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();
            MatrixStack matrixStack = context.matrixStack();

            // 遍历每一个需要渲染的方块
            for (BlockPos pos : blocksToRender) {
                matrixStack.push();
                matrixStack.translate(
                        pos.getX() - cameraPos.getX(),
                        pos.getY() - cameraPos.getY(),
                        pos.getZ() - cameraPos.getZ()
                );

                Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

                // --- [核心修正] 逐边判断逻辑 ---
                // 为了性能，预先计算6个方向的邻居是否存在
                boolean hasNeighborN = blocksToRender.contains(pos.north());
                boolean hasNeighborS = blocksToRender.contains(pos.south());
                boolean hasNeighborE = blocksToRender.contains(pos.east());
                boolean hasNeighborW = blocksToRender.contains(pos.west());
                boolean hasNeighborU = blocksToRender.contains(pos.up());
                boolean hasNeighborD = blocksToRender.contains(pos.down());

                // 定义方块的8个顶点
                float x0 = 0f, y0 = 0f, z0 = 0f;
                float x1 = 1f, y1 = 1f, z1 = 1f;

                // 绘制4条垂直边 (Y轴)
                // 左前 (West-North)
                if (!hasNeighborW || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x0, y0, z0, x0, y1, z0);
                // 右前 (East-North)
                if (!hasNeighborE || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x1, y0, z0, x1, y1, z0);
                // 左后 (West-South)
                if (!hasNeighborW || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x0, y0, z1, x0, y1, z1);
                // 右后 (East-South)
                if (!hasNeighborE || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x1, y0, z1, x1, y1, z1);

                // 绘制4条水平边 (X轴)
                // 顶前 (Up-North)
                if (!hasNeighborU || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x0, y1, z0, x1, y1, z0);
                // 顶后 (Up-South)
                if (!hasNeighborU || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x0, y1, z1, x1, y1, z1);
                // 底前 (Down-North)
                if (!hasNeighborD || !hasNeighborN) drawLine(positionMatrix, vertexConsumer, x0, y0, z0, x1, y0, z0);
                // 底后 (Down-South)
                if (!hasNeighborD || !hasNeighborS) drawLine(positionMatrix, vertexConsumer, x0, y0, z1, x1, y0, z1);

                // 绘制4条水平边 (Z轴)
                // 顶左 (Up-West)
                if (!hasNeighborU || !hasNeighborW) drawLine(positionMatrix, vertexConsumer, x0, y1, z0, x0, y1, z1);
                // 顶右 (Up-East)
                if (!hasNeighborU || !hasNeighborE) drawLine(positionMatrix, vertexConsumer, x1, y1, z0, x1, y1, z1);
                // 底左 (Down-West)
                if (!hasNeighborD || !hasNeighborW) drawLine(positionMatrix, vertexConsumer, x0, y0, z0, x0, y0, z1);
                // 底右 (Down-East)
                if (!hasNeighborD || !hasNeighborE) drawLine(positionMatrix, vertexConsumer, x1, y0, z0, x1, y0, z1);

                matrixStack.pop();
            }
        });
    }

    /**
     * 辅助方法，在 VertexConsumer 中绘制一条线
     */
    private static void drawLine(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2) {
        consumer.vertex(matrix, x1, y1, z1).color(R, G, B, A).normal(0f, 1f, 0f);
        consumer.vertex(matrix, x2, y2, z2).color(R, G, B, A).normal(0f, 1f, 0f);
    }

    /**
     * 更新需要渲染的方块列表。
     */
    public static void setBlocksToRender(List<BlockPos> blocks) {
        MinecraftClient.getInstance().execute(() -> {
            if (blocks == null || blocks.isEmpty()) {
                blocksToRender = Collections.emptySet();
            } else {
                blocksToRender = new HashSet<>(blocks);
            }
        });
    }

    /**
     * 清除渲染列表
     */
    public static void clear() {
        if (!blocksToRender.isEmpty()) {
            MinecraftClient.getInstance().execute(() -> blocksToRender = Collections.emptySet());
        }
    }
}