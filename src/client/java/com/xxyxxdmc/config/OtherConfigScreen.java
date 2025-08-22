package com.xxyxxdmc.config;

import com.xxyxxdmc.jade.JadeTextMouseGlint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class OtherConfigScreen extends AbstractConfigScreen {
    public OtherConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {
        super(parent, Text.translatable("config.hoshikima.category.other"), HoshikimaConfig.get(), pendingConfig);
    }
    @Override
    protected void initContent() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        // GridWidget.Adder adder = gridWidget.createAdder(2);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.client != null) {
            JadeTextMouseGlint.drawFancyTitle(context, Text.translatable("config.hoshikima.category.features.empty").getString(), (float) this.height / 2 - 50, mouseX, mouseY, this.client.textRenderer, 1.5F);
        }
    }
}
