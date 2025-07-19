package com.xxyxxdmc.init;

import com.mojang.serialization.Codec;
import com.xxyxxdmc.RandomThing;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModDataComponents {
    public static final ComponentType<Integer> FUEL = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(RandomThing.MOD_ID, "fuel"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> POWER = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(RandomThing.MOD_ID, "power"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Boolean> MISSING_PAPER = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(RandomThing.MOD_ID, "missing_paper"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOLEAN)
                    .build()
    );
    public static final ComponentType<Integer> COUNT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(RandomThing.MOD_ID, "ender_pearl_count"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static void register() {

    }
}
