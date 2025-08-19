package com.xxyxxdmc.config;

import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ChainMineConfigScreen extends AbstractConfigScreen {
    private PrecisionSliderWidget slider;
    protected ChainMineConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {
        super(parent, Text.translatable("config.hoshikima.category.chain.mine"), HoshikimaConfig.get(), pendingConfig);
    }
    @Override
    protected void initContent() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(createBooleanButton("config.hoshikima.category.chain.mine", () -> pendingConfig.enableChainMine, value -> pendingConfig.enableChainMine = value));
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

        adder.add(createBooleanButton("config.hoshikima.chain.mine.disable.deep.test",
                () -> pendingConfig.disableLineDeepTest,
                value -> pendingConfig.disableLineDeepTest = value),1);

        adder.add(createSliderWidget("config.hoshikima.chain.mine.skip.blocks", 8, 0, pendingConfig.skipAirBlocksInOnce, pendingConfig, 0), 1);

        adder.add(createSliderWidget("config.hoshikima.chain.mine.skip.blocks.total", 64, 8, pendingConfig.skipAirBlocksInTotal, pendingConfig, 1), 1);

        adder.add(createSliderWidget("config.hoshikima.chain.mine.tool.save", 512, 0, pendingConfig.antiToolBreakValue, pendingConfig, 2), 1);

        adder.add(new IntegerSliderWidget(Text.translatable("config.hoshikima.chain.mine.limit"), pendingConfig.blockChainLimit, 2048, 1, pendingConfig, 3) {
            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (this.isHovered()) {
                    double step;
                    if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                        step = 100.0;
                    } else if (Screen.hasControlDown()) {
                        step = 50.0;
                    } else if (Screen.hasShiftDown()) {
                        step = 10.0;
                    } else {
                        step = 1.0;
                    }

                    double deltaValue = (verticalAmount * step) / (2048 - 1);
                    this.value += deltaValue;

                    this.value = MathHelper.clamp(this.value, 0.0, 1.0);

                    this.updateMessage();
                    this.applyValue();

                    return true;
                }
                return false;
            }
            @Override
            protected void updateMessage() {
                if (this.value == 1.0) this.setMessage(Text.translatable("config.hoshikima.chain.mine.limit.max"));
                else if (this.value == 0.0) this.setMessage(Text.translatable("config.hoshikima.chain.mine.limit.min"));
                else this.setMessage(Text.translatable("config.hoshikima.chain.mine.limit").copy().append(": ").append(String.valueOf(this.getIntegerValue())));
            }
        }, 1);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f); 
        gridWidget.forEachChild(this::addDrawableChild);
    }
}
