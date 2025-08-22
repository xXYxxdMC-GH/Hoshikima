package com.xxyxxdmc.mixin;

import com.xxyxxdmc.init.api.IChainMineState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements IChainMineState {

    @Unique
    private boolean chainMineActive = false;
    @Unique
    private List<BlockPos> pendingBreakList = null;
    @Unique
    private int totalSkipAirs = 0;
    @Unique
    private boolean ableBreak = false;

    @Override
    public boolean isChainMiningActive() {
        return this.chainMineActive;
    }

    @Override
    public void setChainMiningActive(boolean active) {
        this.chainMineActive = active;
    }

    @Override
    public void setPendingBreakList(List<BlockPos> blocks) {
        this.pendingBreakList = blocks;
    }

    @Override
    public List<BlockPos> getPendingBreakList() {
        return this.pendingBreakList;
    }

    @Override
    public void clearPendingBreakList() {
        this.pendingBreakList = null;
    }

    @Override
    public int getTotalSkipAirs() {
        return this.totalSkipAirs;
    }

    @Override
    public void setTotalSkipAirs(int airs) {
        this.totalSkipAirs = airs;
    }

    @Override
    public boolean enableBreak() {
        return this.ableBreak;
    }

    @Override
    public void setAbleBreak(boolean ableBreak) {
        this.ableBreak = ableBreak;
    }
}
