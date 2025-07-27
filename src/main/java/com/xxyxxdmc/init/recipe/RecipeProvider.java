package com.xxyxxdmc.init.recipe;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                RegistryWrapper.Impl<Item> itemLookup = registries.getOrThrow(RegistryKeys.ITEM);
                offerBlasting(
                        List.of(Items.RAW_COPPER_BLOCK),
                        RecipeCategory.MISC,
                        Items.COPPER_BLOCK,
                        6.3f,
                        900,
                        "ore_block_blast"
                );
                offerBlasting(
                        List.of(Items.RAW_IRON_BLOCK),
                        RecipeCategory.MISC,
                        Items.IRON_BLOCK,
                        6.3f,
                        900,
                        "ore_block_blast"
                );
                offerBlasting(
                        List.of(Items.RAW_GOLD_BLOCK),
                        RecipeCategory.MISC,
                        Items.GOLD_BLOCK,
                        6.3f,
                        900,
                        "ore_block_blast"
                );
                offerSmelting(
                        List.of(Items.RAW_COPPER_BLOCK),
                        RecipeCategory.MISC,
                        Items.COPPER_BLOCK,
                        6.3f,
                        900,
                        "ore_block_blast"
                );
                offerSmelting(
                        List.of(Items.RAW_IRON_BLOCK),
                        RecipeCategory.MISC,
                        Items.IRON_BLOCK,
                        6.3f,
                        900,
                        "ore_block_blast"
                );
                offerSmelting(
                        List.of(Items.RAW_GOLD_BLOCK),
                        RecipeCategory.MISC,
                        Items.GOLD_BLOCK,
                        6.3f,
                        900,
                        "ore_block_blast"
                );
            }
        };
    }

    @Override
    public String getName() {
        return "FabricDocsReferenceRecipeProvider";
    }
}
