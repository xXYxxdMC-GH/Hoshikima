package com.xxyxxdmc.init.recipe;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.init.ModItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class HoshikimaRecipeProvider extends FabricRecipeProvider {
    public HoshikimaRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        HoshikimaConfig config = HoshikimaConfig.get();
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                RegistryWrapper.Impl<Item> itemLookup = registries.getOrThrow(RegistryKeys.ITEM);
                createShaped(RecipeCategory.TOOLS, ModItem.EnderPearlBundleRegister.ENDER_PEARL_BUNDLE, 1)
                        .pattern("#C#")
                        .pattern("DBD")
                        .pattern("EEE")
                        .input('#', Items.ENDER_EYE)
                        .input('C', Items.ENDER_CHEST)
                        .input('D', Items.DIAMOND)
                        .input('B', Items.BUNDLE)
                        .input('E', Items.ENDER_PEARL)
                        .criterion(hasItem(Items.BUNDLE), conditionsFromItem(Items.BUNDLE))
                        .offerTo(exporter);
                KeepEnchantShapelessRecipeJsonBuilder.create(
                                RecipeCategory.TOOLS,
                                ModItem.EnderPearlBundleRegister.ENDER_PEARL_BUNDLE
                        )
                        .input(Ingredient.ofItem(ModItem.EnderPearlBundleRegister.ENDER_PEARL_BUNDLE))
                        .criterion(
                                hasItem(ModItem.EnderPearlBundleRegister.ENDER_PEARL_BUNDLE),
                                conditionsFromItem(ModItem.EnderPearlBundleRegister.ENDER_PEARL_BUNDLE)
                        )
                        .offerTo(exporter, "hoshikima:ender_pearl_bundle_empty");
            }
        };
    }

    @Override
    public String getName() {
        return "HoshikimaRecipeProvider";
    }
}
