package com.xxyxxdmc.init;

import com.xxyxxdmc.RandomThing;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> ENDER_ESCAPE = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(RandomThing.MOD_ID, "ender_escape")
    );

    public static void registerEnchantments() {
    }
}
