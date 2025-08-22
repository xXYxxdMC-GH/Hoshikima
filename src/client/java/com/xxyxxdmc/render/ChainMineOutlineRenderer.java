package com.xxyxxdmc.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.xxyxxdmc.Hoshikima;
import com.xxyxxdmc.config.CommonValue;
import com.xxyxxdmc.config.HoshikimaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.*;

public class ChainMineOutlineRenderer {

    private static Set<BlockPos> blocksToRender = Collections.emptySet();

    private static final HoshikimaConfig config = HoshikimaConfig.get();

    private static final RenderLayer CUSTOM_LINE_LAYER = createCustomLineLayer();

    public static void init() {
        WorldRenderEvents.LAST.register(context -> {
            if (blocksToRender.isEmpty()) return;

            VertexConsumerProvider vertexConsumers = context.consumers();
            if (vertexConsumers == null) return;

            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(config.disableLineDeepTest ? CUSTOM_LINE_LAYER : RenderLayer.LINES);

            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();

            MatrixStack matrixStack = context.matrixStack();
            matrixStack.push();
            matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            Matrix4f matrix = matrixStack.peek().getPositionMatrix();

            for (Edge edge : collectExposedEdges(blocksToRender)) drawEdge(vertexConsumer, matrix, edge.p1, edge.p2, config.disableLineDeepTest);

            matrixStack.pop();
            ((VertexConsumerProvider.Immediate) vertexConsumers).draw();
        });
    }

    private static void drawEdge(VertexConsumer vc, Matrix4f matrix, Vec3d from, Vec3d to, boolean disableDepthTest) {
        int color = CommonValue.colors.get(config.lineColor);
        float red = (float) ColorHelper.getRed(color) / 255.0F;
        float green = (float) ColorHelper.getGreen(color) / 255.0F;
        float blue = (float) ColorHelper.getBlue(color) / 255.0F;
        float alpha = (float) ColorHelper.getAlpha(color) / 255.0F;

        if (disableDepthTest) {
            vc.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                    .color(red, green, blue, alpha);
            vc.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                    .color(red, green, blue, alpha);
        } else {
            vc.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
                    .color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
            vc.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
                    .color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        }
    }

    private static RenderLayer createCustomLineLayer() {
        RenderPipeline.Builder pipelineBuilder = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET);

        pipelineBuilder.withLocation("pipeline/chain_lines");
        pipelineBuilder.withVertexShader("core/rendertype_lines");
        pipelineBuilder.withFragmentShader(Identifier.of(Hoshikima.MOD_ID, "rendertype_lines_unaffected"));

        pipelineBuilder.withUniform("LineWidth", UniformType.FLOAT).withUniform("ScreenSize", UniformType.VEC2);
        pipelineBuilder.withBlend(BlendFunction.TRANSLUCENT);
        pipelineBuilder.withCull(false);
        pipelineBuilder.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.LINES);
        pipelineBuilder.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST);

        RenderLayer.MultiPhaseParameters.Builder parametersBuilder = RenderLayer.MultiPhaseParameters.builder();
        parametersBuilder.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1.0f)));
        parametersBuilder.layering(RenderPhase.NO_LAYERING);
        parametersBuilder.lightmap(RenderPhase.DISABLE_LIGHTMAP);
        parametersBuilder.texture(RenderPhase.TextureBase.NO_TEXTURE);
        parametersBuilder.texturing(RenderPhase.Texturing.DEFAULT_TEXTURING);

        return RenderLayer.of(
                "hoshikima_no_deep_test_line",
                256,
                true,
                true,
                pipelineBuilder.build(),
                parametersBuilder.build(false)
        );
    }

    private static Set<Edge> collectExposedEdges(Set<BlockPos> blocks) {
        Set<Edge> edgesToRender = new HashSet<>();
        Set<Edge> edgesToRemove = new HashSet<>();

        for (BlockPos pos : blocks) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                List<Edge> faceEdges = getEdgesForFace(pos, dir);

                if (blocks.contains(neighbor)) {
                    for (Edge edge : faceEdges) {
                        edgesToRemove.add(edge.normalized());
                    }
                } else {
                    for (Edge edge : faceEdges) {
                        edgesToRender.add(edge.normalized());
                    }
                }
            }
        }

        edgesToRender.removeAll(edgesToRemove);
        return edgesToRender;
    }

    private static List<Edge> getEdgesForFace(BlockPos pos, Direction dir) {
        Vec3d[] corners = getFaceCorners(pos, dir);
        return List.of(
                new Edge(corners[0], corners[1]),
                new Edge(corners[1], corners[2]),
                new Edge(corners[2], corners[3]),
                new Edge(corners[3], corners[0])
        );
    }

    private static Vec3d[] getFaceCorners(BlockPos pos, Direction dir) {
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return switch (dir) {
            case UP -> new Vec3d[]{
                    new Vec3d(x, y + 1, z),
                    new Vec3d(x + 1, y + 1, z),
                    new Vec3d(x + 1, y + 1, z + 1),
                    new Vec3d(x, y + 1, z + 1)
            };
            case DOWN -> new Vec3d[]{
                    new Vec3d(x, y, z),
                    new Vec3d(x + 1, y, z),
                    new Vec3d(x + 1, y, z + 1),
                    new Vec3d(x, y, z + 1)
            };
            case NORTH -> new Vec3d[]{
                    new Vec3d(x, y, z),
                    new Vec3d(x + 1, y, z),
                    new Vec3d(x + 1, y + 1, z),
                    new Vec3d(x, y + 1, z)
            };
            case SOUTH -> new Vec3d[]{
                    new Vec3d(x, y, z + 1),
                    new Vec3d(x + 1, y, z + 1),
                    new Vec3d(x + 1, y + 1, z + 1),
                    new Vec3d(x, y + 1, z + 1)
            };
            case WEST -> new Vec3d[]{
                    new Vec3d(x, y, z),
                    new Vec3d(x, y, z + 1),
                    new Vec3d(x, y + 1, z + 1),
                    new Vec3d(x, y + 1, z)
            };
            case EAST -> new Vec3d[]{
                    new Vec3d(x + 1, y, z),
                    new Vec3d(x + 1, y, z + 1),
                    new Vec3d(x + 1, y + 1, z + 1),
                    new Vec3d(x + 1, y + 1, z)
            };
        };
    }

    private record Edge(Vec3d p1, Vec3d p2) {
        public Edge normalized() {
            return p1.x < p2.x || (p1.x == p2.x && p1.y < p2.y) ||
                    (p1.x == p2.x && p1.y == p2.y && p1.z < p2.z)
                    ? this : new Edge(p2, p1);
        }
    }

    public static void setBlocksToRender(List<BlockPos> blocks) {
        MinecraftClient.getInstance().execute(() -> blocksToRender = (blocks == null || blocks.isEmpty()) ? Collections.emptySet() : new HashSet<>(blocks));
    }
}
