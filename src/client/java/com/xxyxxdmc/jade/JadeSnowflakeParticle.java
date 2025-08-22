package com.xxyxxdmc.jade;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * @author Snownee
 * @author xXYxxdMC
 * @Source Github <a href="https://github.com/Snownee/Jade/blob/1.21.6-fabric/src/main/java/snownee/jade/gui/HomeConfigScreen.java">...</a>
 * @License CC BY-NC-SA 4.0 International
 * @Description  This code block is adapted and modified from the original work.
 * @Note  This portion remains under the original license and is intended for non-commercial use only.
 */
public class JadeSnowflakeParticle {
    public float age;
    private final String text;
    private float x;
    public float y;
    private float motionX;
    private float motionY;
    private final int color;
    private final float scale;
    private final float friction = 0.98F;
    public float gravity = 0.98F;
    public boolean needToStop = false;
    public boolean needHasAlpha = false;
    public JadeSnowflakeParticle(String text, float x, float y, float motionX, float motionY, int color, float scale, float age) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.motionX = motionX;
        this.motionY = motionY;
        this.color = color;
        this.scale = scale;
        this.age = age;
        this.needToStop = true;
        this.needHasAlpha = true;
    }
    public JadeSnowflakeParticle(String text, float x, float y, float motionX, float motionY, int color, float scale) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.motionX = motionX;
        this.motionY = motionY;
        this.color = color;
        this.scale = scale;
        this.age = 100;
    }

    public void tick(float partialTicks) {
        this.x += this.motionX * partialTicks;
        this.y += this.motionY * partialTicks;
        this.motionY += this.gravity * partialTicks;
        if (needToStop) {
            motionX *= friction;
            motionY *= friction;
        }
    }

    public void render(DrawContext graphics, TextRenderer font) {
        float alpha = Math.max(0, Math.min(1.0F, age / 100.0F));
        graphics.getMatrices().push();
        graphics.getMatrices().translate(x, y, 0);
        graphics.getMatrices().scale(scale, scale, scale);
        int colorWithAlpha = (int) (alpha * 255.0F) << 24 | (this.color & 0x00FFFFFF);
        graphics.drawTextWithShadow(font, text, 0, 0, needHasAlpha ? colorWithAlpha : this.color);
        graphics.getMatrices().pop();
    }
}
