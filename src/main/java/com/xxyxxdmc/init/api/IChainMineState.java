package com.xxyxxdmc.init.api;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IChainMineState {
    boolean isChainMiningActive();
    void setChainMiningActive(boolean active);
    void setPendingBreakList(List<BlockPos> blocks);
    List<BlockPos> getPendingBreakList();
    void clearPendingBreakList();
    int getTotalSkipAirs();
    void setTotalSkipAirs(int airs);
    boolean enableBreak();
    void setAbleBreak(boolean able);
}
