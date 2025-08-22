package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.Hoshikima;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChainMineKeyPressPayload(boolean isPressed, int direction) implements CustomPayload {
    public static final CustomPayload.Id<ChainMineKeyPressPayload> ID = new CustomPayload.Id<>(Identifier.of(Hoshikima.MOD_ID, "chain_mine_key_state"));

    public static final PacketCodec<PacketByteBuf, ChainMineKeyPressPayload> CODEC = PacketCodec.of(
            ChainMineKeyPressPayload::write,
            ChainMineKeyPressPayload::new
    );

    public ChainMineKeyPressPayload(PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readInt());
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(isPressed);
        buf.writeInt(direction);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
