package com.xxyxxdmc.jade;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * @author Snownee
 * @author xXYxxdMC
 * @Source Github <a href="https://github.com/Snownee/Jade/blob/1.21.6-fabric/src/main/java/snownee/jade/gui/HomeConfigScreen.java">...</a>
 * @License CC BY-NC-SA 4.0 International
 * @Description  This code block is adapted and modified from the original work.
 * @Note  This portion remains under the original license and is intended for non-commercial use only.
 */
public class JadeTextMouseGlint {
    public static void drawFancyTitle(DrawContext guiGraphics, String text, float y, float mouseX, float mouseY, TextRenderer textRenderer, float scale) {
        float screenWidth = guiGraphics.getScaledWindowWidth();
        float textWidth = textRenderer.getWidth(text) * scale;
        float centeredX = (screenWidth - textWidth) / 2.0F;

        int color = 11184810;

        float scaledMouseY = mouseY / scale;

        float glint2Strength = 1.0F - MathHelper.clamp(Math.abs(scaledMouseY - y / scale) / 20.0F, 0.0F, 1.0F);

        MutableText component = Text.empty();
        MutableInt curX = new MutableInt();
        TextVisitFactory.visitFormatted(text, Style.EMPTY, (index, style, codePoint) -> {
            String s = Character.toString(codePoint);
            int width = textRenderer.getWidth(s);
            int curXVal = curX.getValue();
            curX.add(width);
            curXVal += width / 2;

            float scaledMouseX = mouseX / scale;
            float dist = Math.abs((float)curXVal + centeredX / scale - scaledMouseX);

            float colorMul = 0.65F + MathHelper.clamp(1.0F - dist / 20.0F, 0.0F, 1.0F) * 0.35F * glint2Strength;
            int originalColor = style.getColor() == null ? 11184810 : style.getColor().getRgb();
            component.append(Text.literal(s).fillStyle(style).withColor(ColorHelper.scaleRgb(originalColor, colorMul)));
            return true;
        });

        guiGraphics.getMatrices().push();
        guiGraphics.getMatrices().scale(scale, scale, scale);
        guiGraphics.getMatrices().translate(centeredX / scale, y / scale, 0.0F);
        guiGraphics.drawTextWithShadow(textRenderer, component, 0, 0, color);
        guiGraphics.getMatrices().pop();
    }
}
