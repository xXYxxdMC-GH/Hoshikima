package com.xxyxxdmc.docs.datagen;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.init.enchantment.EnchantmentGenerator;
import com.xxyxxdmc.init.recipe.HoshikimaRecipeProvider;
import com.xxyxxdmc.init.tag.ModItemTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

@SuppressWarnings("unused")
public class HoshikimaDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        HoshikimaConfig config = HoshikimaConfig.get();
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(EnchantmentGenerator::new);
        pack.addProvider(HoshikimaRecipeProvider::new);
        pack.addProvider(ModItemTagProvider::new);
    }
}