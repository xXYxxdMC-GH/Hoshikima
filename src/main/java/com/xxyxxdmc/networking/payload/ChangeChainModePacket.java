package com.xxyxxdmc.networking.payload;

import com.xxyxxdmc.Hoshikima;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ChangeChainModePacket {
    public static final Identifier ID = Identifier.of(Hoshikima.MOD_ID, "change_chain_mode");

//    public static FriendlyByteBuf createBuffer(boolean up) {
//        FriendlyByteBuf buf = PacketByteBufs.create();
//        buf.writeBoolean(up);
//        return buf;
//    }
//
//    public static void send(boolean up) {
//        ServerPlayNetworking.send(Minecraft.getInstance().player, ID, createBuffer(up));
//    }
}
