package com.xxyxxdmc.init;

import com.xxyxxdmc.Hoshikima;
import com.xxyxxdmc.init.item.EnderPearlBundle;
import com.xxyxxdmc.init.item.FireworkThruster;
import com.xxyxxdmc.init.item.LargeBucket;
import com.xxyxxdmc.init.item.MultiFluidBucket;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItem {
    public static final Item ENDER_PEARL_BUNDLE = register("ender_pearl_bundle", EnderPearlBundle::new, new Item.Settings());
    public static final Item FIREWORK_THRUSTER = register("firework_thruster", FireworkThruster::new, new Item.Settings());
    public static final Item LARGE_BUCKET = register("large_bucket", LargeBucket::new, new Item.Settings());
    public static final Item MULTI_FLUID_BUCKET = register("multi_fluid_bucket", MultiFluidBucket::new, new Item.Settings());
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Hoshikima.MOD_ID, name));
        Item item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }
    public static void initialize() {

    }
}
