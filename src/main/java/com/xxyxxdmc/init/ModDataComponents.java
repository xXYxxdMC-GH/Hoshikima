package com.xxyxxdmc.init;

import com.mojang.serialization.Codec;
import com.xxyxxdmc.Hoshikima;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModDataComponents {
    public static final ComponentType<Integer> FUEL = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "fuel"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> POWER = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "power"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Boolean> MISSING_PAPER = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "missing_paper"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOLEAN)
                    .build()
    );
    public static final ComponentType<Integer> COUNT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "ender_pearl_count"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> FLUID_TYPE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "fluid_type"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> LAVA_CAPACITY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "lava_capacity"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> WATER_CAPACITY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "water_capacity"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> SNOW_CAPACITY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "snow_capacity"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> SPARE_CAPACITY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "spare_capacity"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> MODE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "mode"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<Integer> FILL_TYPE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "fill_type"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );
    public static final ComponentType<List<String>> ENTITIES_IN_BUCKET = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "entities_in_bucket"),
            ComponentType.<List<String>>builder()
                    .codec(Codec.list(Codec.STRING))
                    .packetCodec(PacketCodecs.codec(Codec.list(Codec.STRING)))
                    .build()
    );

    public static void register() {

    }
}
