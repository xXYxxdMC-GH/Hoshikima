package com.xxyxxdmc.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FeatureConfigScreen extends AbstractConfigScreen {

    protected FeatureConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {
        super(parent, Text.translatable("config.hoshikima.category.features"), HoshikimaConfig.get(), pendingConfig);
    }

    @Override
    protected void initContent() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        // adder.add(new TextWidget(Text.translatable("config.hoshikima.category.features.empty").formatted(Formatting.GRAY), this.textRenderer), 2);
        adder.add(ButtonWidget.builder(
                    Text.translatable("config.hoshikima.category.chain.mine"),
                    button -> this.client.setScreen(new ChainMineConfigScreen(this, pendingConfig))).build(), 1);
        adder.add(ButtonWidget.builder(
                    Text.translatable("config.hoshikima.category.other"), 
                    button -> this.client.setScreen(new OtherConfigScreen(this, pendingConfig))).build(), 1);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }
}
