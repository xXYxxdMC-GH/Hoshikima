package com.xxyxxdmc.init.callback;

import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface IChainMineState {
    boolean isChainMiningActive();
    void setChainMiningActive(boolean active);
    void setPendingBreakList(List<BlockPos> blocks);
    List<BlockPos> getPendingBreakList();
    void clearPendingBreakList();
    Direction getDirection();
    void setDirection(Direction direction);
}
