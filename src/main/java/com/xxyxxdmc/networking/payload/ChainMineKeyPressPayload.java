package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.Hoshikima;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ChainMineKeyPressPayload(boolean isPressed) implements CustomPayload {
    public static final CustomPayload.Id<ChainMineKeyPressPayload> ID = new CustomPayload.Id<>(Hoshikima.CHAIN_MINE_PACKET_ID);

    public static final PacketCodec<PacketByteBuf, ChainMineKeyPressPayload> CODEC = PacketCodec.of(
            ChainMineKeyPressPayload::write,
            ChainMineKeyPressPayload::new
    );

    public ChainMineKeyPressPayload(PacketByteBuf buf) {
        this(buf.readBoolean());
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(isPressed);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}