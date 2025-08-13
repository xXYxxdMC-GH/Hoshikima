package com.xxyxxdmc.init.recipe;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KeepEnchantShapelessRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<Ingredient> inputs = new ArrayList<>();
    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap<>();
    @Nullable
    private String group;

    public KeepEnchantShapelessRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public static KeepEnchantShapelessRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return new KeepEnchantShapelessRecipeJsonBuilder(category, output, 1);
    }

    public KeepEnchantShapelessRecipeJsonBuilder input(Ingredient ingredient) {
        this.inputs.add(ingredient);
        return this;
    }

    @Override
    public KeepEnchantShapelessRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        this.advancementBuilder.put(name, criterion);
        return this;
    }

    @Override
    public KeepEnchantShapelessRecipeJsonBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        Identifier recipeId = recipeKey.getValue();
        this.validate(recipeId);

        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.advancementBuilder.forEach(builder::criterion);

        KeepEnchantmentShapelessRecipe recipe = new KeepEnchantmentShapelessRecipe(
                Objects.toString(this.group, ""),
                CraftingRecipeJsonBuilder.toCraftingCategory(this.category),
                new ItemStack(this.output, this.count),
                this.inputs
        );

        exporter.accept(
                recipeKey,
                recipe,
                builder.build(recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/"))
        );
    }

    private void validate(Identifier recipeId) {
        if (this.advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId + ". You must add a criterion.");
        }
        if (this.inputs.isEmpty()) {
            throw new IllegalStateException("No ingredients defined for shapeless recipe " + recipeId);
        }
    }
}