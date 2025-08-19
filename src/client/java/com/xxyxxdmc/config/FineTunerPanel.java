package com.xxyxxdmc.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class FineTunerPanel {
    final FineTunerSlider hundredSlider;
    final FineTunerSlider tenSlider;
    final FineTunerSlider oneSlider;
    private final PrecisionSliderWidget parent;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    boolean visible = false;

    private ClickableWidget selectedChild = null;

    public FineTunerPanel(int x, int y, int width, int height, PrecisionSliderWidget parent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;

        this.hundredSlider = new FineTunerSlider(x + 5, y + 5, width - 10, 15, Text.literal("百位"), 0.0, this::updateParentValue);
        this.tenSlider = new FineTunerSlider(x + 5, y + 25, width - 10, 15, Text.literal("十位"), 0.0, this::updateParentValue);
        this.oneSlider = new FineTunerSlider(x + 5, y + 45, width - 10, 15, Text.literal("个位"), 0.0, this::updateParentValue);
    }

    private void updateParentValue() {
        int hundreds = (int) Math.round(this.hundredSlider.getValue() * 9);
        int tens = (int) Math.round(this.tenSlider.getValue() * 9);
        int ones = (int) Math.round(this.oneSlider.getValue() * 9);
        if (hundreds < 0) hundreds = 0;
        else if (hundreds > 9) hundreds = 9;
        if (tens < 0) tens = 0;
        else if (tens > 9) tens = 9;
        if (ones < 0) ones = 0;
        else if (ones > 9) ones = 9;
        int newValue = hundreds * 100 + tens * 10 + ones;

        this.parent.mainValue = MathHelper.clamp((newValue - this.parent.min) / (this.parent.max - this.parent.min), 0.0, 1.0);
        this.parent.updateMessage();
        this.parent.valueConsumer.accept(this.parent.getIntegerValue());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0xA0000000);
        this.hundredSlider.render(context, mouseX, mouseY, delta);
        this.tenSlider.render(context, mouseX, mouseY, delta);
        this.oneSlider.render(context, mouseX, mouseY, delta);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        if (hundredSlider.mouseClicked(mouseX, mouseY, button)) {
            this.selectedChild = hundredSlider;
            return true;
        }
        if (tenSlider.mouseClicked(mouseX, mouseY, button)) {
            this.selectedChild = tenSlider;
            return true;
        }
        if (oneSlider.mouseClicked(mouseX, mouseY, button)) {
            this.selectedChild = oneSlider;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.selectedChild != null) {
            return this.selectedChild.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.selectedChild != null) {
            boolean handled = this.selectedChild.mouseReleased(mouseX, mouseY, button);
            this.selectedChild = null;
            return handled;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        if (hundredSlider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (tenSlider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return oneSlider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}

class FineTunerSlider extends SliderWidget {
    private final Runnable action;
    private final Text text;

    public FineTunerSlider(int x, int y, int width, int height, Text text, double value, Runnable action) {
        super(x, y, width, height, text, value);
        this.text = text;
        this.action = action;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.literal(text.getString() + ": " + (int)Math.round(this.value * 9)));
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setValueFromMouse(mouseX);
        if (action != null) action.run();
    }

    @Override
    protected void applyValue() {
        if (action != null) action.run();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            this.setValueFromMouse(mouseX);
            this.updateMessage();
            if (action != null) action.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.updateMessage();
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.isHovered()) {
            this.value += verticalAmount * 0.05;
            this.value = MathHelper.clamp(this.value, 0.0, 1.0);
            this.updateMessage();
            this.applyValue();
            return true;
        }
        return false;
    }

    private void setValueFromMouse(double mouseX) {
        this.setValue((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8));
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}