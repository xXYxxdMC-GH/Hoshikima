package com.xxyxxdmc.init.item.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.List;

import static com.xxyxxdmc.init.ModDataComponents.*;

public class LargeBucketTooltip {
    public static void append(ItemStack stack, List<Text> text) {
        int currentFluid = stack.getOrDefault(FLUID_TYPE, 0);
        int mode = stack.getOrDefault(MODE, 1);
        int size = stack.getOrDefault(ENTITIES_SIZE, 0);
        int maxCapacity = 8;

        if (mode == 1) text.add(Text.translatable("tooltip.hoshikima.mode").append(Text.literal(": ").formatted(Formatting.GRAY)).append(Text.translatable("tooltip.hoshikima.load").withColor(new Color(38, 153, 0).getRGB())).formatted(Formatting.GRAY));
        else text.add(Text.translatable("tooltip.hoshikima.mode").append(Text.literal(": ").formatted(Formatting.GRAY)).append(Text.translatable("tooltip.hoshikima.unload").withColor(new Color(180, 0, 0).getRGB())).formatted(Formatting.GRAY));

        if (currentFluid != 0) {
            int currentCapacity = stack.getOrDefault(currentFluid == 1 ? WATER_CAPACITY : currentFluid == 2 ? LAVA_CAPACITY : SNOW_CAPACITY, 0);
            if (currentFluid == 1) text.add(Text.translatable("tooltip.hoshikima.water").withColor(new Color(0, 116, 216).getRGB()));
            else if (currentFluid == 2) text.add(Text.translatable("tooltip.hoshikima.lava").withColor(new Color(221, 76, 0).getRGB()));
            else text.add(Text.translatable("tooltip.hoshikima.powder_snow").withColor(new Color(255, 255, 255).getRGB()));
            text.add(Text.translatable("tooltip.hoshikima.capacity")
                    .append(Text.literal(": " + currentCapacity + " / " + maxCapacity))
                    .formatted(Formatting.GRAY));
        } else {
            text.add(Text.translatable("tooltip.hoshikima.empty").formatted(Formatting.GRAY));
        }

        if (size > 0) {
            if (Screen.hasShiftDown()) {
                text.add(Text.translatable("tooltip.hoshikima.entity", size).formatted(Formatting.GRAY));

                List<NbtCompound> entities = stack.getOrDefault(ENTITIES_IN_BUCKET, List.of());
                for (NbtCompound entityNbt : entities) {
                    Text entityName;
                    if (entityNbt.contains("CustomName")) {
                        entityName = Text.of(entityNbt.getString("CustomName", "Unknown") + " (" + Text.translatable("entity." + entityNbt.getString("id", "minecraft:cod").replace(':', '.')).getString() + ")");
                    } else {
                        String entityId = entityNbt.getString("id", "minecraft:cod");
                        if (entityId.equals("hoshikima:empty")) entityName = Text.translatable("tooltip.hoshikima.empty");
                        else {
                            String translationKey = "entity." + entityId.replace(':', '.');
                            entityName = Text.translatable(translationKey);
                        }
                    }
                    if (entityNbt.contains("Variant") ) {
                        if (entityNbt.getString("id", "minecraft:cod").equals("minecraft:axolotl")) {
                            Color color = switch (entityNbt.getInt("Variant", 0)) {
                                case 1 -> new Color(162, 122, 86);
                                case 2 -> new Color(255, 209, 27);
                                case 3 -> new Color(176, 213, 252);
                                case 4 -> new Color(134, 144, 245);
                                default -> new Color(251, 193, 227);
                            };
                            text.add(Text.literal("  - ").formatted(Formatting.DARK_GRAY).append(entityName.copy().withColor(color.getRGB())).append(Screen.hasControlDown() ? Text.empty().append(" (❤ × ").append(String.valueOf(entityNbt.getFloat("Health", 1.0F))).append(")").append((entityNbt.getListOrEmpty("active_effects").toString().contains("minecraft:poison")) ? Text.of(" (💀)").copy().withColor(new Color(33, 141, 0).getRGB()) : Text.empty()).append((entityNbt.getListOrEmpty("active_effects").toString().contains("minecraft:regeneration")) ? Text.of(" (❤)").copy().withColor(new Color(255, 114, 114).getRGB()) : Text.empty()) : Text.empty()));
                        } else if (entityNbt.getString("id", "minecraft:cod").equals("minecraft:tropical_fish")) {
                            TropicalFishEntity.Variant variant = entityNbt.get("Variant", TropicalFishEntity.Variant.CODEC).orElse(TropicalFishEntity.DEFAULT_VARIANT);
                            Color baseColor = new Color(variant.baseColor().getMapColor().color);
                            Color patternColor = new Color(variant.patternColor().getMapColor().color);
                            String name = entityName.getString();
                            Text firstWord = Text.of(name.contains(" ") ? name.split(" ")[0] : name.substring(0, name.length() / 2)).copy().withColor(baseColor.getRGB());
                            Text lastWord = Text.of(name.substring(firstWord.getString().length())).copy().withColor(patternColor.getRGB());
                            text.add(Text.literal("  - ").formatted(Formatting.DARK_GRAY).append(firstWord).append(lastWord).append(Screen.hasControlDown() ? Text.empty().append(" (❤ × ").append(String.valueOf(entityNbt.getFloat("Health", 1.0F))).append(")") : Text.empty()));
                        }
                    } else if (entityNbt.getString("id", "minecraft:cod").equals("hoshikima:empty")) text.add(Text.literal("  - ").append(entityName).formatted(Formatting.DARK_GRAY));
                    else text.add(Text.literal("  - ").formatted(Formatting.DARK_GRAY).append(entityName.copy().formatted(Formatting.GRAY)).append(Screen.hasControlDown() ? Text.empty().append(" (❤ × ").append(String.valueOf(entityNbt.getFloat("Health", 1.0F))).append(")") : Text.empty()));
                }
            } else {
                text.add(Text.translatable("tooltip.hoshikima.entity", size).append(Text.translatable("tooltip.hoshikima.detail")).formatted(Formatting.GRAY));
            }
        }
    }
}