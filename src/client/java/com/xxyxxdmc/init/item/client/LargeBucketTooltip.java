package com.xxyxxdmc.init.item.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static com.xxyxxdmc.init.ModDataComponents.*;

public class LargeBucketTooltip {
    public static void append(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int currentFluid = stack.getOrDefault(FLUID_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        int size = stack.getOrDefault(ENTITIES_IN_BUCKET, List.of()).size();
        int maxCapacity = 8;

        if (mode == 1) textConsumer.accept(Text.translatable("tooltip.hoshikima.mode").append(Text.literal(": ").formatted(Formatting.GRAY)).append(Text.translatable("tooltip.hoshikima.load").withColor(new Color(38, 153, 0).getRGB())).formatted(Formatting.GRAY));
        else textConsumer.accept(Text.translatable("tooltip.hoshikima.mode").append(Text.literal(": ").formatted(Formatting.GRAY)).append(Text.translatable("tooltip.hoshikima.unload").withColor(new Color(180, 0, 0).getRGB())).formatted(Formatting.GRAY));

        if (currentFluid != 0) {
            int currentCapacity = stack.getOrDefault(currentFluid == 1 ? WATER_CAPACITY : currentFluid == 2 ? LAVA_CAPACITY : SNOW_CAPACITY, 0);
            if (currentFluid == 1) textConsumer.accept(Text.translatable("tooltip.hoshikima.water").withColor(new Color(0, 116, 216).getRGB()));
            else if (currentFluid == 2) textConsumer.accept(Text.translatable("tooltip.hoshikima.lava").withColor(new Color(221, 76, 0).getRGB()));
            else textConsumer.accept(Text.translatable("tooltip.hoshikima.powder_snow").withColor(new Color(255, 255, 255).getRGB()));
            textConsumer.accept(Text.translatable("tooltip.hoshikima.capacity")
                    .append(Text.literal(": " + currentCapacity + " / " + maxCapacity))
                    .formatted(Formatting.GRAY));
        } else {
            textConsumer.accept(Text.translatable("tooltip.hoshikima.empty").formatted(Formatting.GRAY));
        }

        if (size > 0) {
            if (Screen.hasShiftDown()) {
                textConsumer.accept(Text.translatable("tooltip.hoshikima.entity", size).formatted(Formatting.GRAY));

                List<NbtCompound> entities = stack.getOrDefault(ENTITIES_IN_BUCKET, List.of());
                for (NbtCompound entityNbt : entities) {
                    Text entityName;
                    if (entityNbt.contains("CustomName")) {
                        entityName = Text.of(entityNbt.getString("CustomName").get() + " (" + Text.translatable("entity." + entityNbt.getString("id").get().replace(':', '.')).getString() + ")");
                    } else {
                        String entityId = entityNbt.getString("id").get();
                        String translationKey = "entity." + entityId.replace(':', '.');
                        entityName = Text.translatable(translationKey);
                    }
                    if (entityNbt.contains("Variant") && entityNbt.getString("id").get().equals("minecraft:axolotl")) {
                        Color color = switch (entityNbt.getInt("Variant").get()) {
                            case 1 -> new Color(162,122,86);
                            case 2 -> new Color(255,209,27);
                            case 3 -> new Color(176,213,252);
                            case 4 -> new Color(134,144,245);
                            default -> new Color(251, 193, 227);
                        };
                        textConsumer.accept(Text.literal("  - ").formatted(Formatting.DARK_GRAY).append(entityName.copy().withColor(color.getRGB())));
                    } else textConsumer.accept(Text.literal("  - ").append(entityName).formatted(Formatting.DARK_GRAY));
                }
            } else {
                textConsumer.accept(Text.translatable("tooltip.hoshikima.entity", size).append(Text.translatable("tooltip.hoshikima.detail")).formatted(Formatting.GRAY));
            }
        }
    }
}