package com.xxyxxdmc.config;

import java.awt.Color;
import java.util.ArrayList;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text; 

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ChainMineConfigScreen extends AbstractConfigScreen {
    protected ChainMineConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {
        super(parent, Text.translatable("config.hoshikima.category.chain.mine"), HoshikimaConfig.get(), pendingConfig);
    }
    @Override
    protected void initContent() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        List<Text> chainModeText = new ArrayList<>();
        chainModeText.add(Text.translatable("config.hoshikima.chain.mine.mode.one"));
        chainModeText.add(Text.translatable("config.hoshikima.chain.mine.mode.two"));
        adder.add(createIntegerButton("config.hoshikima.chain.mine.chain.mode", () -> pendingConfig.chainMode, value -> pendingConfig.chainMode = value, chainModeText), 1);
        adder.add(createBooleanButton("config.hoshikima.chain.mine.exp.gather", () -> pendingConfig.enableExpGather, value -> pendingConfig.enableExpGather = value));

        List<String> keys = List.of(
            "red", "green", "blue", "yellow", "purple", "cyan", "white", "black"
        );
        List<Text> colorTexts = new ArrayList<>();
        for (String key: keys) colorTexts.add(Text.translatable("config.hoshikima.chain.mine.color." + key).withColor(CommonValue.colors.get(keys.indexOf(key))));   
        colorTexts.add(Text.of("ホシノの色").copy().withColor(CommonValue.colors.getLast()));

        adder.add(createIntegerButton(
                "config.hoshikima.chain.mine.color", 
                () -> pendingConfig.lineColor , 
                value -> pendingConfig.lineColor = value, colorTexts));    

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f); 
        gridWidget.forEachChild(this::addDrawableChild);
    }
}
