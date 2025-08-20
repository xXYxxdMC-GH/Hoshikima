package com.xxyxxdmc.config;

import java.awt.*;
import java.util.ArrayList;

import com.xxyxxdmc.jade.JadeSnowflakeParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class ChainMineConfigScreen extends AbstractConfigScreen {
    private IntegerSliderWidget slider;
    private final Random random = Random.create();
    private final List<JadeSnowflakeParticle> particles = new ArrayList<>();
    private int lastStyle;
    private int lastColor;
    private boolean shouldGenerateParticles = false;
    private boolean shouldGenerateHeartParticles = false;
    protected ChainMineConfigScreen(Screen parent, HoshikimaConfig pendingConfig) {
        super(parent, Text.translatable("config.hoshikima.category.chain.mine"), HoshikimaConfig.get(), pendingConfig);
        this.lastStyle = pendingConfig.hudDisplayWay;
        this.lastColor = pendingConfig.lineColor;
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
        adder.add(createIntegerButton("config.hoshikima.chain.mine.chain.mode", () -> pendingConfig.chainMode, value -> pendingConfig.chainMode = value, chainModeText, "config.hoshikima.chain.mine.chain.mode.tooltip"), 1);
        adder.add(createBooleanButton("config.hoshikima.chain.mine.exp.gather", () -> pendingConfig.enableExpGather, value -> pendingConfig.enableExpGather = value, "config.hoshikima.chain.mine.exp.gather.tooltip"));

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
                value -> pendingConfig.disableLineDeepTest = value,
                "config.hoshikima.chain.mine.disable.deep.test.tooltip"),1);

        adder.add(createSliderWidget("config.hoshikima.chain.mine.skip.blocks", 8, 0, pendingConfig.skipAirBlocksInOnce, pendingConfig, 0, "config.hoshikima.chain.mine.skip.blocks.tooltip"), 1);

        adder.add(createSliderWidget("config.hoshikima.chain.mine.skip.blocks.total", 64, 8, pendingConfig.skipAirBlocksInTotal, pendingConfig, 1, "config.hoshikima.chain.mine.skip.blocks.total.tooltip"), 1);

        adder.add(createSliderWidget("config.hoshikima.chain.mine.tool.save", 512, 0, pendingConfig.antiToolBreakValue, pendingConfig, 2, "config.hoshikima.chain.mine.tool.save.tooltip"), 1);

        this.slider = new IntegerSliderWidget(Text.translatable("config.hoshikima.chain.mine.limit"), pendingConfig.blockChainLimit, 2048, 1, pendingConfig, 3) {
            {
                setTooltip(Tooltip.of(Text.translatable("config.hoshikima.chain.mine.limit.tooltip")));
            }
            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (this.isHovered()) {
                    double step;
                    if (Screen.hasControlDown() && Screen.hasShiftDown()) {
                        step = 100.0;
                        setTooltip(Tooltip.of(Text.translatable("config.hoshikima.chain.mine.limit.too.fast").withColor(new Color(152, 0, 202).getRGB())));
                    } else if (Screen.hasControlDown()) {
                        step = 50.0;
                        setTooltip(Tooltip.of(Text.translatable("config.hoshikima.chain.mine.limit.fast").withColor(new Color(0, 208, 255).getRGB())));
                    } else if (Screen.hasShiftDown()) {
                        step = 10.0;
                        setTooltip(Tooltip.of(Text.translatable("config.hoshikima.chain.mine.limit.normal").withColor(new Color(17, 255, 0).getRGB())));
                    } else {
                        step = 1.0;
                        setTooltip(Tooltip.of(Text.translatable("config.hoshikima.chain.mine.limit.slow").withColor(new Color(191, 166, 133).getRGB())));
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
        };
        adder.add(this.slider, 1);

        Text waysKey = switch (pendingConfig.hudDisplayWay) {
            case 1 -> Text.translatable("config.hoshikima.chain.mine.hud.way.jade").append(Text.of("Jade").copy().withColor(new Color(206, 255, 225).getRGB()).append(Text.of("❤").copy().withColor(new Color(255, 118, 195).getRGB())));
            default -> Text.translatable("config.hoshikima.chain.mine.hud.way.ftb");
        };

        adder.add(ButtonWidget.builder(Text.translatable("config.hoshikima.chain.mine.hud.way").append(": ").append(waysKey), b -> {
            pendingConfig.hudDisplayWay++;
            if (pendingConfig.hudDisplayWay > 1) pendingConfig.hudDisplayWay = 0;
            Text newWaysKey = switch (pendingConfig.hudDisplayWay) {
                case 1 -> Text.translatable("config.hoshikima.chain.mine.hud.way.jade").append(Text.of("Jade ").copy().withColor(new Color(206, 255, 225).getRGB()).append(Text.of("❤").copy().withColor(new Color(255, 118, 195).getRGB())));
                default -> Text.translatable("config.hoshikima.chain.mine.hud.way.ftb");
            };
            b.setMessage(Text.translatable("config.hoshikima.chain.mine.hud.way").append(": ").append(newWaysKey));
        }).width(150).build(), 1);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 32, this.width, this.height, 0.5f, 0.0f); 
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (slider != null && !slider.isHovered()) slider.setTooltip(Tooltip.of(Text.translatable("config.hoshikima.chain.mine.limit.tooltip")));
        if (pendingConfig.hudDisplayWay == 1 && lastStyle != 1) {
            shouldGenerateParticles = true;
        }
        if (pendingConfig.lineColor == 8 && lastColor != 8) {
            shouldGenerateHeartParticles = true;
        }

        lastStyle = pendingConfig.hudDisplayWay;
        lastColor = pendingConfig.lineColor;

        if (shouldGenerateParticles) {
            for (int i = 0; i < 15; i++) {
                int alpha = random.nextInt(20) + 220;
                int color = 0x00ADD8E6;
                color |= (alpha << 24);

                float initialMotionY = -4.0f - random.nextFloat() * 1.5f;
                float initialMotionX = (i > 7 ? 1.0f : -1.0f) * random.nextFloat() * 2.0f;

                float startX = (float) this.width / 2 + 75 + random.nextBetween(-75, 75);
                float startY = (float) this.height / 2 + 20;

                JadeSnowflakeParticle particle = new JadeSnowflakeParticle(
                        "❄",
                        startX,
                        startY,
                        initialMotionX,
                        initialMotionY,
                        color,
                        0.5F + (random.nextFloat() * 0.6F)
                );

                particle.gravity = 0.6F;

                particles.add(particle);
            }
            shouldGenerateParticles = false;
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
        }

        if (shouldGenerateHeartParticles) {
            for (int i = 0; i < 15; i++) {
                int alpha = random.nextInt(20) + 220;
                int color = 0x00FFC0CB;
                color |= (alpha << 24);

                float initialMotionY = random.nextBetween(-1, 1) * 1f;
                float initialMotionX = (i > 7 ? 1.0f : -1.0f) * random.nextBetween(1,2) * 0.5f;

                float startX = (float) this.width / 2 + 75 + random.nextBetween(-75, 75);
                float startY = (float) this.height / 2 - 55;

                JadeSnowflakeParticle particle = new JadeSnowflakeParticle(
                        "❤",
                        startX,
                        startY,
                        initialMotionX,
                        initialMotionY,
                        color,
                        0.5F + (random.nextFloat() * 0.8F),
                        100.0F
                );

                particles.add(particle);
                particle.gravity = 0F;
            }
            shouldGenerateHeartParticles = false;
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F));

        }

        float deltaTicks = MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();
        particles.removeIf(p -> {
            p.tick(deltaTicks);
            if (p.y > this.height + 20 || p.age <= 0) {
                return true;
            }
            p.render(context, this.textRenderer);
            p.age--;
            return false;
        });
    }
}
