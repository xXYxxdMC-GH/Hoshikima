package com.xxyxxdmc.init.tag;

import com.xxyxxdmc.Hoshikima;
import com.xxyxxdmc.init.ModItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider<Item> {

    public static final TagKey<Item> ENDER_PEARL_BUNDLE_ENCHANTABLE =
            TagKey.of(RegistryKeys.ITEM, Identifier.of(Hoshikima.MOD_ID, "enchantable/ender_pearl_bundle"));

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, RegistryKeys.ITEM, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(ENDER_PEARL_BUNDLE_ENCHANTABLE)
                .add(ModItem.EnderPearlBundleRegister.ENDER_PEARL_BUNDLE)
                .setReplace(false);
    }
}