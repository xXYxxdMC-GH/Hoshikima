package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.Hoshikima;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record QueryChainMineBlocksPacket(BlockPos pos, boolean isPressed, int direction) implements CustomPayload {
    public static final CustomPayload.Id<QueryChainMineBlocksPacket> ID =
            new CustomPayload.Id<>(Identifier.of(Hoshikima.MOD_ID, "query_chain_mine_blocks"));

    public static final PacketCodec<PacketByteBuf, QueryChainMineBlocksPacket> CODEC = PacketCodec.of(
            QueryChainMineBlocksPacket::write,
            QueryChainMineBlocksPacket::new
    );

    public QueryChainMineBlocksPacket(PacketByteBuf buf) {
        this(buf.readBlockPos(), buf.readBoolean(), buf.readInt());
    }

    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.isPressed);
        buf.writeInt(this.direction);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}