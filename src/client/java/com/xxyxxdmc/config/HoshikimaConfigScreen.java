package com.xxyxxdmc.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.net.URI;
import java.net.URISyntaxException;

public class HoshikimaConfigScreen extends AbstractConfigScreen {

    private static final String HOSHIKIMA_PART_STR = "Hoshikima";
    private static final String CONFIGURATION_PART_STR = Text.of(" ").copy().append(Text.translatable("config.hoshikima.title")).getString();
    private static final Text JAPANESE_TITLE = Text.of("ホシキマ");
    private static final Text HOSHIKIMA_TEXT = Text.of(HOSHIKIMA_PART_STR);

    private static final int WHITE_COLOR_RGB = 0xFFFFFF;
    private static final int PINK_COLOR_RGB = 0xFF9CF7;

    private static final float TITLE_Y = 25.0f;
    private static final float WIPE_START_DISTANCE = 40.0f;
    private static final float FADE_START_DISTANCE = 10.0f;
    private static final float INSTANT_SWITCH_DISTANCE = 5.0f;

    private static final float MIN_SCALE = 1.8f;
    private static final float MAX_SCALE = 2.2f;
    private static final float JAPANESE_EXTRA_SCALE = 1.5f;

    public HoshikimaConfigScreen(Screen parent) {
        super(parent, Text.literal(HOSHIKIMA_PART_STR + CONFIGURATION_PART_STR));
    }

    @Override
    protected void initContent() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4);
        GridWidget.Adder adder = gridWidget.createAdder(2);

        ButtonWidget itemButton = ButtonWidget.builder(
                        Text.translatable("config.hoshikima.category.items"),
                        button -> this.client.setScreen(new ItemConfigScreen(this, this.pendingConfig))
                )
                .width(100)
                .tooltip(Tooltip.of(Text.translatable("config.hoshikima.category.items.tooltip")))
                .build();

        ButtonWidget featureButton = ButtonWidget.builder(
                        Text.translatable("config.hoshikima.category.features"),
                        button -> this.client.setScreen(new FeatureConfigScreen(this, this.pendingConfig))
                )
                .width(100)
                .tooltip(Tooltip.of(Text.translatable("config.hoshikima.category.features.tooltip")))
                .build();

        adder.add(itemButton);
        adder.add(featureButton);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0.5f);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            final float titleX = this.width / 2.0f;
            final double distance = Math.hypot(mouseX - titleX, mouseY - TITLE_Y - 10);

            if (distance <= INSTANT_SWITCH_DISTANCE) {
                float scale = MAX_SCALE * JAPANESE_EXTRA_SCALE;
                float textWidth = this.textRenderer.getWidth(JAPANESE_TITLE);
                float scaledWidth = textWidth * scale;
                float scaledHeight = this.textRenderer.fontHeight * scale;

                float left = titleX - (scaledWidth / 2.0f);
                float right = titleX + (scaledWidth / 2.0f);
                float bottom = TITLE_Y + scaledHeight;

                if (mouseX >= left && mouseX <= right && mouseY >= TITLE_Y && mouseY <= bottom) {
                    final String url = "https://www.curseforge.com/minecraft/mc-mods/hoshikima";
                    this.client.setScreen(new ConfirmLinkScreen(confirmed -> {
                        if (confirmed) {
                            try {
                                Util.getOperatingSystem().open(new URI(url));
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                        this.client.setScreen(this);
                    }, url, true));
                    this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTitle(DrawContext context, int mouseX, int mouseY) {
        final float titleX = this.width / 2.0f;
        final double distance = Math.hypot(mouseX - titleX, mouseY - TITLE_Y - 5);

        context.getMatrices().push();
        context.getMatrices().translate(titleX, TITLE_Y, 0.0);

        if (distance > WIPE_START_DISTANCE) {
            context.getMatrices().scale(MIN_SCALE, MIN_SCALE, 1.0f);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, 0, 0, 0xFFFFFFFF);

        } else if (distance > FADE_START_DISTANCE) {
            float progress = 1.0f - (float) MathHelper.clamp((distance - FADE_START_DISTANCE) / (WIPE_START_DISTANCE - FADE_START_DISTANCE), 0.0, 1.0);
            int charsToHide = (int) (CONFIGURATION_PART_STR.length() * progress);
            String visiblePart = CONFIGURATION_PART_STR.substring(0, CONFIGURATION_PART_STR.length() - charsToHide);
            Text currentTitle = Text.literal(HOSHIKIMA_PART_STR + visiblePart);
            float scale = MathHelper.lerp(progress, MIN_SCALE, MAX_SCALE);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.drawCenteredTextWithShadow(this.textRenderer, currentTitle, 0, 0, 0xFFFFFFFF);

        } else if (distance > INSTANT_SWITCH_DISTANCE) {
            float progress = 1.0f - (float) MathHelper.clamp((distance - INSTANT_SWITCH_DISTANCE) / (FADE_START_DISTANCE - INSTANT_SWITCH_DISTANCE), 0.0, 1.0);
            float scale = MathHelper.lerp(progress, MAX_SCALE, MAX_SCALE * JAPANESE_EXTRA_SCALE);
            context.getMatrices().scale(scale, scale, 1.0f);

            float hoshikimaAlpha = 1.0f - progress;
            if (hoshikimaAlpha > 0.01f) {
                int hoshikimaColor = ((int) (hoshikimaAlpha * 255) << 24) | WHITE_COLOR_RGB;
                context.drawCenteredTextWithShadow(this.textRenderer, HOSHIKIMA_TEXT, 0, 0, hoshikimaColor);
            }

            if (progress > 0.01f) {
                int japaneseColor = ((int) (progress * 255) << 24) | PINK_COLOR_RGB;
                context.drawCenteredTextWithShadow(this.textRenderer, JAPANESE_TITLE, 0, 0, japaneseColor);
            }
        } else {
            float scale = MAX_SCALE * JAPANESE_EXTRA_SCALE;
            context.getMatrices().scale(scale, scale, 1.0f);
            context.drawCenteredTextWithShadow(this.textRenderer, JAPANESE_TITLE, 0, 0, 0xFF000000 | PINK_COLOR_RGB);
        }

        context.getMatrices().pop();
    }
}
