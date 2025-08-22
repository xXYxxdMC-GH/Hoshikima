package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.Hoshikima;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateChainModePayload(int newMode) implements CustomPayload {
    public static final CustomPayload.Id<UpdateChainModePayload> ID = new CustomPayload.Id<>(Identifier.of(Hoshikima.MOD_ID, "update_chain_mode"));

    public static final PacketCodec<PacketByteBuf, UpdateChainModePayload> CODEC = PacketCodec.of(
            UpdateChainModePayload::write,
            UpdateChainModePayload::new
    );

    public UpdateChainModePayload(PacketByteBuf buf) {
        this(buf.readInt());
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(newMode);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
