package com.xxyxxdmc.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ItemConfigScreen extends AbstractConfigScreen {

    protected ItemConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {

        super(parent, Text.translatable("config.hoshikima.category.items"), HoshikimaConfig.get(), pendingConfig);
    }

    @Override
    protected void initContent() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(createBooleanButton("item.hoshikima.ender_pearl_bundle", () -> pendingConfig.enableEnderPearlBundle, value -> pendingConfig.enableEnderPearlBundle = value), 1);
        adder.add(createBooleanButton("item.hoshikima.firework_thruster", () -> pendingConfig.enableFireworkThruster, value -> pendingConfig.enableFireworkThruster = value), 1);
        adder.add(createBooleanButton("item.hoshikima.large_bucket", () -> pendingConfig.enableLargeBucket, value -> pendingConfig.enableLargeBucket = value), 1);
        adder.add(createBooleanButton("item.hoshikima.multi_fluid_bucket", () -> pendingConfig.enableMultiFluidBucket, value -> pendingConfig.enableMultiFluidBucket = value), 1);
        adder.add(createBooleanButton("item.hoshikima.rotten_flesh_cluster", () -> pendingConfig.enableRottenFleshCluster, value -> pendingConfig.enableRottenFleshCluster = value), 1);

        Text warningText = Text.translatable("config.hoshikima.restart_required").formatted(Formatting.YELLOW);
        adder.add(new TextWidget(warningText, this.textRenderer), 2, adder.copyPositioner().marginTop(24));

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }
}