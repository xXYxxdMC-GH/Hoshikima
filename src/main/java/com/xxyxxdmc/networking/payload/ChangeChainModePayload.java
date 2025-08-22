package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.Hoshikima;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChangeChainModePayload(boolean value) implements CustomPayload {
    public static final CustomPayload.Id<ChangeChainModePayload> ID = new CustomPayload.Id<>(Identifier.of(Hoshikima.MOD_ID, "change_chain_mode"));

    public static final PacketCodec<PacketByteBuf, ChangeChainModePayload> CODEC = PacketCodec.of(
            ChangeChainModePayload::write,
            ChangeChainModePayload::new
    );

    public ChangeChainModePayload(PacketByteBuf buf) {
        this(buf.readBoolean());
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(value);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
