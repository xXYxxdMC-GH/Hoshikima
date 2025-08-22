package com.xxyxxdmc.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.navigation.GuiNavigationType;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class PrecisionSliderWidget extends ClickableWidget implements Element {
    double mainValue;
    final FineTunerPanel fineTunerPanel;
    final double min;
    final double max;
    final Consumer<Integer> valueConsumer;
    private final Text message;
    private boolean sliderFocused;

    private static final Identifier TEXTURE = Identifier.ofVanilla("widget/slider");
    private static final Identifier HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/slider_highlighted");
    private static final Identifier HANDLE_TEXTURE = Identifier.ofVanilla("widget/slider_handle");
    private static final Identifier HANDLE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/slider_handle_highlighted");

    public PrecisionSliderWidget(int x, int y, int width, int height, Text message, double min, double max, int initialValue, Consumer<Integer> valueConsumer) {
        super(x, y, width, height, message);
        this.message = message;
        this.min = min;
        this.max = max;
        this.valueConsumer = valueConsumer;
        this.mainValue = (initialValue - min) / (max - min);

        this.fineTunerPanel = new FineTunerPanel(x, y + height + 2, 100, 65, this);
        this.updateMessage();
        this.syncValueToFineTuner();
    }

    public int getIntegerValue() {
        return (int) Math.round(MathHelper.lerp(this.mainValue, min, max));
    }

    void updateMessage() {
        this.setMessage(this.message.copy().append(": ").append(String.valueOf(this.getIntegerValue())));
    }

    void syncValueToFineTuner() {
        int value = this.getIntegerValue();
        this.fineTunerPanel.hundredSlider.setValue((double)(value / 100) / 9.0);
        this.fineTunerPanel.tenSlider.setValue((double)((value % 100) / 10) / 9.0);
        this.fineTunerPanel.oneSlider.setValue((double)(value % 10) / 9.0);
    }

    public boolean isHovered() {
        return this.hovered || this.fineTunerPanel.visible;
    }


    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.drawGuiTexture(RenderLayer::getGuiTextured, this.getTexture(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.getWhite(this.alpha));
        context.drawGuiTexture(RenderLayer::getGuiTextured, this.getHandleTexture(), this.getX() + (int)(this.mainValue * (double)(this.width - 8)), this.getY(), 8, this.getHeight(), ColorHelper.getWhite(this.alpha));
        int textColor;

        if (this.active && !this.fineTunerPanel.isMouseOver(mouseX, mouseY) && !this.fineTunerPanel.visible) {
            textColor = 16777215;
            textColor |= (MathHelper.ceil(this.alpha * 255.0F) << 24);
        } else {
            textColor = 10526880;
            int semiTransparentAlpha = 128;
            textColor |= (semiTransparentAlpha << 24);
        }

        this.fineTunerPanel.visible = this.fineTunerPanel.isMouseOver(mouseX,mouseY);

        this.drawScrollableText(context, minecraftClient.textRenderer, 2, textColor);
    }

    private Identifier getTexture() {
        return this.isNarratable() && this.isFocused() && !this.sliderFocused ? HIGHLIGHTED_TEXTURE : TEXTURE;
    }

    private Identifier getHandleTexture() {
        return !this.isNarratable() || !this.hovered && !this.sliderFocused ? HANDLE_TEXTURE : HANDLE_HIGHLIGHTED_TEXTURE;
    }

    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.sliderFocused = false;
        } else {
            GuiNavigationType guiNavigationType = MinecraftClient.getInstance().getNavigationType();
            if (guiNavigationType == GuiNavigationType.MOUSE || guiNavigationType == GuiNavigationType.KEYBOARD_TAB) {
                this.sliderFocused = true;
            }
        }
    }

    public boolean isAreaHovered(double mouseX, double mouseY) {
        return this.isHovered() || this.fineTunerPanel.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setValueFromMouse(mouseX);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    private void setValueFromMouse(double mouseX) {
        this.mainValue = MathHelper.clamp((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8), 0.0, 1.0);
        this.updateMessage();
        this.valueConsumer.accept(this.getIntegerValue());
        this.syncValueToFineTuner();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.fineTunerPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.fineTunerPanel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.fineTunerPanel.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.fineTunerPanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        if (this.isAreaHovered(mouseX, mouseY) && !this.fineTunerPanel.isMouseOver(mouseX, mouseY)) {
            this.mainValue += verticalAmount * 0.05;
            this.mainValue = MathHelper.clamp(this.mainValue, 0.0, 1.0);
            this.updateMessage();
            this.valueConsumer.accept(this.getIntegerValue());
            this.syncValueToFineTuner();
            return true;
        }
        return false;
    }
}