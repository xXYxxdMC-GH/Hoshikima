package com.xxyxxdmc.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderPhase.Layering;
import net.minecraft.client.render.RenderPhase.Lightmap;
import net.minecraft.client.render.RenderPhase.LineWidth;
import net.minecraft.client.render.RenderPhase.Target;
import net.minecraft.client.render.RenderPhase.TextureBase;
import net.minecraft.client.render.RenderPhase.Texturing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.config.CommonValue;

import net.minecraft.util.Identifier;
import java.util.*;

public class ChainMineOutlineRenderer {

    private static Set<BlockPos> blocksToRender = Collections.emptySet();
    public static boolean disableDepthTest = true;

    private static final HoshikimaConfig config = HoshikimaConfig.get();

    public static void init() {
        WorldRenderEvents.LAST.register(context -> {
            if (blocksToRender.isEmpty()) return;

            VertexConsumerProvider vertexConsumerProvider = context.consumers();
            if (vertexConsumerProvider == null) return;

            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();
            Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(config.disableLineDeepTest ? getRenderLayer() : RenderLayer.LINES);
            for (Edge edge : collectExposedEdges(blocksToRender)) {
                Vec3d from = edge.p1.subtract(cameraPos);
                Vec3d to = edge.p2.subtract(cameraPos);
                Vec3d[] adjusted = adjustLineEndpoints(from, to);
                drawEdge(vertexConsumer, matrix, adjusted[0], adjusted[1]);
            }
        });
    }

    private static void drawEdge(VertexConsumer vc, Matrix4f matrix, Vec3d from, Vec3d to) {
        vc.vertex(matrix, (float) from.x, (float) from.y, (float) from.z)
          .color(CommonValue.colors.get(config.lineColor)).normal(0f, 1f, 0f);
        vc.vertex(matrix, (float) to.x, (float) to.y, (float) to.z)
          .color(CommonValue.colors.get(config.lineColor)).normal(0f, 1f, 0f);
    }

    private static RenderLayer getRenderLayer() {
        RenderLayer noDeepTestLayer = RenderLayer.of("hoshikima_no_deep_test_line",
        256,
        RenderPipeline.builder()
            .withVertexShader(Identifier.of("minecraft", "rendertype_lines.vsh"))
            .withFragmentShader(Identifier.of("minecraft", "rendertype_lines.fsh"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build(),
            RenderLayer.MultiPhaseParameters.builder()
            .layering(Layering.NO_LAYERING)
            .lightmap(Lightmap.DISABLE_LIGHTMAP)
            .lineWidth(LineWidth.FULL_LINE_WIDTH)
            .target(Target.OUTLINE_TARGET)
            .texture(TextureBase.NO_TEXTURE)
            .texturing(Texturing.DEFAULT_TEXTURING)
            .build(false)
        );
        return noDeepTestLayer;
    }

    private static Vec3d[] adjustLineEndpoints(Vec3d from, Vec3d to) {
        Vec3d dir = to.subtract(from).normalize();
        double distance = from.distanceTo(to);
        double scale = 0.002 + 0.001 * (1.0 / Math.max(0.1, distance));
        Vec3d offset = dir.multiply(scale);
        return new Vec3d[]{from.add(offset), to.subtract(offset)};
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
        MinecraftClient.getInstance().execute(() -> {
            blocksToRender = (blocks == null || blocks.isEmpty()) ? Collections.emptySet() : new HashSet<>(blocks);
        });
    }

    public static void clear() {
        MinecraftClient.getInstance().execute(() -> blocksToRender = Collections.emptySet());
    }
}
