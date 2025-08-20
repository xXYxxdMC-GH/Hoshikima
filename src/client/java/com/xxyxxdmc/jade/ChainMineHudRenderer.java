package com.xxyxxdmc.jade;

import com.xxyxxdmc.key.HoshikimaKeyBind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import snownee.jade.overlay.DisplayHelper;

public class ChainMineHudRenderer {

    public static int totalBlocks;
    public static int skippedAirs;

    public static void render(DrawContext context, RenderTickCounter tickDelta) {
        if (!HoshikimaKeyBind.CHAIN_MINE_KEY.isPressed() || MinecraftClient.getInstance().player == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int padding = 5;
        int backgroundAlpha = 0x80000000;
        int textX = screenWidth - 100;
        int textY = screenHeight - 50;

        Text totalBlocksText = Text.translatable("hud.hoshikima.chain.mine.total.chain.block")
                .formatted(Formatting.GOLD)
                .append(Text.literal(": " + totalBlocks).formatted(Formatting.WHITE));

        Text skippedAirsText = Text.translatable("hud.hoshikima.chain.mine.skip.blocks.total")
                .formatted(Formatting.GOLD)
                .append(Text.literal(": " + skippedAirs).formatted(Formatting.WHITE));

        int totalBlocksWidth = textRenderer.getWidth(totalBlocksText);
        int skippedAirsWidth = textRenderer.getWidth(skippedAirsText);

        int column1Width = totalBlocksWidth;
        int column2Width = skippedAirsWidth;
        int gap = 10;
        int totalWidth = column1Width + gap + column2Width + padding * 2;
        int totalHeight = textRenderer.fontHeight * 2 + padding * 2;

        int windowX = screenWidth - totalWidth - 10;
        int windowY = screenHeight - totalHeight - 10;
        DisplayHelper.fill(context, windowX, windowY, windowX + totalWidth, windowY + totalHeight, backgroundAlpha);

        context.drawText(textRenderer, totalBlocksText, windowX + padding, windowY + padding, 0xFFFFFFFF, true);

        context.drawText(textRenderer, skippedAirsText, windowX + padding, windowY + padding + textRenderer.fontHeight + 2, 0xFFFFFFFF, true);

        int lineX = windowX + padding + column1Width + gap / 2;
        int lineY_start = windowY + padding;
        int lineY_end = windowY + totalHeight - padding;
        DisplayHelper.fill(context, lineX, lineY_start, lineX + 1, lineY_end, 0xFF808080);
    }
}
