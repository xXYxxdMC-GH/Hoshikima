package com.xxyxxdmc.config;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final HoshikimaConfig config;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("config.hoshikima.title"));
        this.parent = parent;
        this.config = HoshikimaConfig.get();
    }

    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(createBooleanButton("item.hoshikima.ender_pearl_bundle", () -> config.enableEnderPearlBundle, value -> config.enableEnderPearlBundle = value));
        adder.add(createBooleanButton("item.hoshikima.firework_thruster", () -> config.enableFireworkThruster, value -> config.enableFireworkThruster = value));
        adder.add(createBooleanButton("item.hoshikima.large_bucket", () -> config.enableLargeBucket, value -> config.enableLargeBucket = value));
        adder.add(createBooleanButton("item.hoshikima.multi_fluid_bucket", () -> config.enableMultiFluidBucket, value -> config.enableMultiFluidBucket = value));
        adder.add(createBooleanButton("item.hoshikima.rotten_flesh_cluster", () -> config.enableRottenFleshCluster, value -> config.enableRottenFleshCluster = value));

        adder.add(new TextWidget(Text.empty(), this.textRenderer), 2);
        Text warningText = Text.translatable("config.hoshikima.restart_required").formatted(Formatting.YELLOW);
        adder.add(new TextWidget(warningText, this.textRenderer), 2);
        adder.add(new TextWidget(Text.empty(), this.textRenderer), 2, gridWidget.copyPositioner().marginTop(8));

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).build());
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.client.setScreen(this.parent)).build());

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f);

        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void close() {
        this.config.save();
        this.client.setScreen(this.parent);
    }

    private ButtonWidget createBooleanButton(String translationKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        Text optionText = Text.translatable(translationKey);

        Supplier<Text> messageSupplier = () -> optionText.copy().append(": ").append(getter.get() ? ScreenTexts.ON : ScreenTexts.OFF);

        return ButtonWidget.builder(
                messageSupplier.get(),
                button -> {
                    boolean newValue = !getter.get();
                    setter.accept(newValue);
                    button.setMessage(messageSupplier.get());
                }
        ).build();
    }
}