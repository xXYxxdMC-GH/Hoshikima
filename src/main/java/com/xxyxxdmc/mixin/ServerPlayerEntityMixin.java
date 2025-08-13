package com.xxyxxdmc.mixin;

import com.mojang.authlib.GameProfile;
import com.xxyxxdmc.init.callback.IChainMineState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements IChainMineState {

    @Unique
    private boolean hoshikima_chainMineActive = false;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public boolean hoshikima_isChainMiningActive() {
        return this.hoshikima_chainMineActive;
    }

    @Override
    public void hoshikima_setChainMiningActive(boolean active) {
        this.hoshikima_chainMineActive = active;
    }
}