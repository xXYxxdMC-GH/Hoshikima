package com.xxyxxdmc.init.recipe;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipe {
    public static final RecipeSerializer<KeepEnchantmentShapelessRecipe> KEEP_ENCHANTMENT_SHAPELESS_SERIALIZER = new KeepEnchantmentShapelessRecipe.Serializer();

    public static void register() {
        Registry.register(
                Registries.RECIPE_SERIALIZER,
                Identifier.of("hoshikima", "crafting_shapeless_keep_enchantment"),
                KEEP_ENCHANTMENT_SHAPELESS_SERIALIZER
        );
    }
}
