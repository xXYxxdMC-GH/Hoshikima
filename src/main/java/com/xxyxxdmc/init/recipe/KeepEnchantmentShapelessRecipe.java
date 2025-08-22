package com.xxyxxdmc.init.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.Optional;

public class KeepEnchantmentShapelessRecipe extends ShapelessRecipe {
    private final ItemStack recipeResult;
    private final List<Ingredient> recipeIngredients;
    public KeepEnchantmentShapelessRecipe(String group, CraftingRecipeCategory category, ItemStack result, List<Ingredient> ingredients) {
        super(group, category, result, ingredients);
        this.recipeResult = result;
        this.recipeIngredients = ingredients;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer<ShapelessRecipe>)(RecipeSerializer<?>) ModRecipe.KEEP_ENCHANTMENT_SHAPELESS_SERIALIZER;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        final ItemStack originalResult = super.craft(craftingRecipeInput, wrapperLookup);
        if (originalResult.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Optional<ItemEnchantmentsComponent> enchantmentsToKeep = craftingRecipeInput.getStacks().stream()
                .filter(stack -> !stack.isEmpty() && stack.getItem() == originalResult.getItem())
                .map(stack -> stack.get(DataComponentTypes.ENCHANTMENTS))
                .filter(enchantments -> enchantments != null && !enchantments.isEmpty())
                .findFirst();

        enchantmentsToKeep.ifPresent(enchantments -> originalResult.set(DataComponentTypes.ENCHANTMENTS, enchantments));

        return originalResult;
    }


    public ItemStack getResultForCodec() {
        return this.recipeResult;
    }
    public List<Ingredient> getIngredients() {
        return this.recipeIngredients;
    }

    public static class Serializer implements RecipeSerializer<KeepEnchantmentShapelessRecipe> {
        private static final MapCodec<KeepEnchantmentShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                        CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(ShapelessRecipe::getCategory),
                        ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(KeepEnchantmentShapelessRecipe::getResultForCodec),
                        Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(KeepEnchantmentShapelessRecipe::getIngredients)
                ).apply(instance, KeepEnchantmentShapelessRecipe::new)
        );

        private static final PacketCodec<RegistryByteBuf, KeepEnchantmentShapelessRecipe> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ShapelessRecipe::getGroup,
                CraftingRecipeCategory.PACKET_CODEC, ShapelessRecipe::getCategory,
                ItemStack.PACKET_CODEC, KeepEnchantmentShapelessRecipe::getResultForCodec,
                Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), KeepEnchantmentShapelessRecipe::getIngredients,
                KeepEnchantmentShapelessRecipe::new
        );

        @Override
        public MapCodec<KeepEnchantmentShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, KeepEnchantmentShapelessRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}