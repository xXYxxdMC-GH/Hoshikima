package com.xxyxxdmc.jade;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
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
    public Text getNarration() {
        return Text.of("");
    }

    @Override
    public void render(DrawContext guiGraphics, int x, int y, float maxX) {
        guiGraphics.fill(x, y, x + this.width, y + this.height, this.color);
    }
}
