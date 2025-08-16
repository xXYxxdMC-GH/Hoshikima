package com.xxyxxdmc.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text;

public class OtherConfigScreen extends AbstractConfigScreen {
    public OtherConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {
        super(parent, Text.translatable("config.hoshikima.category.other"), HoshikimaConfig.get(), pendingConfig);
    }
    @Override
    protected void initContent() {                      GridWidget gridWidget = new GridWidget();                                                   gridWidget.getMainPositioner().margin(4, 4, 4, 0);                                          GridWidget.Adder adder = gridWidget.createAdder(2);                                         //List<Text> chainModeText = new ArrayList<>();                                             //adder.add(createIntegerButton(translationKey:"", () -> pendingConfig.chainMode, value -> pendingConfig.chainMode = value, chainModeText), 1);
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }
}
