package com.xxyxxdmc.config;

import java.util.ArrayList;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text; 

import java.util.List;

public class ChainMineConfigScreen extends AbstractConfigScreen {
    protected ChainMineConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {
        super(parent, Text.translatable(""), HoshikimaConfig.get(), pendingConfig);
    }
    @Override
    protected void initContent() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        List<Text> chainModeText = new ArrayList<>();
        adder.add(createIntegerButton("", () -> pendingConfig.chainMode, value -> pendingConfig.chainMode = value, chainModeText), 1);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f);                                                     gridWidget.forEachChild(this::addDrawableChild);
    }
}
