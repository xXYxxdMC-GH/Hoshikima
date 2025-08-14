package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.callback.IChainMineState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements IChainMineState {

    @Unique
    private boolean chainMineActive = false;

    @Override
    public boolean isChainMiningActive() {
        return this.chainMineActive;
    }

    @Override
    public void setChainMiningActive(boolean active) {
        this.chainMineActive = active;
    }
}