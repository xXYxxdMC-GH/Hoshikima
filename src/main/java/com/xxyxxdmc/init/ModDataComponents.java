package com.xxyxxdmc.init;

import com.mojang.serialization.Codec;
import com.xxyxxdmc.Hoshikima;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

@SuppressWarnings("EmptyMethod")
public class ModDataComponents {
    public static final ComponentType<Integer> FUEL = registerInt("fuel");
    public static final ComponentType<Integer> POWER = registerInt("power");
    public static final ComponentType<Integer> COUNT = registerInt("ender_pearl_count");
    public static final ComponentType<Integer> FLUID_TYPE = registerInt("fluid_type");
    public static final ComponentType<Integer> LAVA_CAPACITY = registerInt("lava_capacity");
    public static final ComponentType<Integer> WATER_CAPACITY = registerInt("water_capacity");
    public static final ComponentType<Integer> SNOW_CAPACITY = registerInt("snow_capacity");
    public static final ComponentType<Integer> SPARE_CAPACITY = registerInt("spare_capacity");
    public static final ComponentType<Integer> MODE = registerInt("mode");
    public static final ComponentType<Integer> FILL_TYPE = registerInt("fill_type");
    public static final ComponentType<Integer> ENTITIES_SIZE = registerInt("entities_size");

    public static final ComponentType<Boolean> MISSING_PAPER = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "missing_paper"),
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOLEAN)
                    .build()
    );
    public static final ComponentType<List<NbtCompound>> ENTITIES_IN_BUCKET = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "entities_in_bucket"),
            ComponentType.<List<NbtCompound>>builder()
                    .codec(NbtCompound.CODEC.listOf())
                    .packetCodec(PacketCodecs.NBT_COMPOUND.collect(PacketCodecs.toList()))
                    .build()
    );
    public static final ComponentType<List<ItemStack>> PENDING_DROPS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(Hoshikima.MOD_ID, "pending_drops"),
            ComponentType.<List<ItemStack>>builder()
                    .codec(ItemStack.CODEC.listOf())
                    .build()
    );
    private static ComponentType<Integer> registerInt(String path) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.of(Hoshikima.MOD_ID, path),
                ComponentType.<Integer>builder()
                        .codec(Codec.INT)
                        .packetCodec(PacketCodecs.VAR_INT)
                        .build()
        );
    }

    public static void register() {

    }
}
