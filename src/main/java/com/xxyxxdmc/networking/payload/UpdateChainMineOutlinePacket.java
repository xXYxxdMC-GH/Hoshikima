package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.Hoshikima;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record UpdateChainMineOutlinePacket(List<BlockPos> blocks, int chainMode, int totalSkipAirs) implements CustomPayload {
    public static final CustomPayload.Id<UpdateChainMineOutlinePacket> ID =
            new CustomPayload.Id<>(Identifier.of(Hoshikima.MOD_ID, "update_chain_mine_outline"));

    public static final PacketCodec<PacketByteBuf, UpdateChainMineOutlinePacket> CODEC = PacketCodec.of(
            UpdateChainMineOutlinePacket::write,
            UpdateChainMineOutlinePacket::new
    );

    public UpdateChainMineOutlinePacket(PacketByteBuf buf) {
        this(
                buf.readList(packetByteBuf -> packetByteBuf.readBlockPos()),
                buf.readInt(),
                buf.readInt()
        );
    }

    public void write(PacketByteBuf buf) {
        buf.writeCollection(this.blocks, (packetByteBuf, blockPos) -> packetByteBuf.writeBlockPos(blockPos));
        buf.writeInt(this.chainMode);
        buf.writeInt(this.totalSkipAirs);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
