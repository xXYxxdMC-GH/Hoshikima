package com.xxyxxdmc.jade;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.Element;

public class SolidColorElement extends Element {

    private final int width;
    private final int height;
    private final int color;

    public SolidColorElement(int width, int height, int color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    @Override
    public Vec2f getSize() {
        return new Vec2f(this.width, this.height);
    }

    @Override
    public void render(DrawContext guiGraphics, float x, float y, float maxX, float maxY) {
        guiGraphics.fill((int) x, (int) y, (int) x + this.width, (int) y + this.height, this.color);
    }

    public @Nullable String getMessage() {
        return this.getString();
    }

    public String getString() {
        return this.toString();
    }
}
