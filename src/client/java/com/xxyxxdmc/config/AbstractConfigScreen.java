package com.xxyxxdmc.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractConfigScreen extends Screen {
    protected final Screen parent;
    protected final HoshikimaConfig config;
    protected final HoshikimaConfig pendingConfig;

    protected AbstractConfigScreen(Screen parent, Text title) {
        this(parent, title, HoshikimaConfig.get(), new HoshikimaConfig(HoshikimaConfig.get()));
    }

    protected AbstractConfigScreen(Screen parent, Text title, HoshikimaConfig config, HoshikimaConfig pendingConfig) {
        super(title);
        this.parent = parent;
        this.config = config;
        this.pendingConfig = pendingConfig;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.close())
                .dimensions(this.width / 2 - 92, this.height - 28, 90, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
                    this.config.apply(this.pendingConfig);
                    this.config.save();
                    this.close();
                })
                .dimensions(this.width / 2 + 2, this.height - 28, 90, 20)
                .build());

        this.initContent();
    }

    protected abstract void initContent();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderTitle(context, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world == null) {
            this.renderPanoramaBackground(context, delta);
            context.fill(0, 0, this.width, this.height, 0, 0x40000000);
        } else {
            this.renderInGameBackground(context);
        }
    }

    protected void renderTitle(DrawContext context, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2.0, 20.0, 0.0);
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, 0, 0, 0xFFFFFF);
        context.getMatrices().pop();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    protected ButtonWidget createBooleanButton(String translationKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        Text optionText = Text.translatable(translationKey);
        Supplier<Text> messageSupplier = () -> optionText.copy().append(": ").append(getter.get() ? ScreenTexts.ON : ScreenTexts.OFF);

        return ButtonWidget.builder(
                messageSupplier.get(),
                button -> {
                    boolean newValue = !getter.get();
                    setter.accept(newValue);
                    button.setMessage(messageSupplier.get());
                }
        ).width(150).build();
    }
    protected ButtonWidget createIntegerButton(String translationKey, Supplier<Integer> getter, Consumer<Integer> setter, List<Text> options) {
        Text optionText = Text.translatable(translationKey);                                        Supplier<Text> messageSupplier = () -> optionText.copy().append(options.get(getter.get()));                                 return ButtonWidget.builder(                        messageSupplier.get(),
                button -> {
                    int newValue = (getter.get() + 1 >= options.size()) ? 0 : getter.get() + 1;
                    setter.accept(newValue);
                    button.setMessage(messageSupplier.get());
                }
        ).width(150).build();
    }
}
