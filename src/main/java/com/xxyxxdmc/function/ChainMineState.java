package com.xxyxxdmc.function;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChainMineState {
    private static final ThreadLocal<Boolean> isChainMining = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<List<ItemStack>> capturedDrops = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Integer> capturedXp = ThreadLocal.withInitial(() -> 0);

    public static boolean isChainMining() {
        return isChainMining.get();
    }

    public static void setChainMining(boolean active) {
        isChainMining.set(active);
    }

    public static void captureDrop(ItemStack stack) {
        capturedDrops.get().add(stack);
    }

    public static List<ItemStack> getCapturedDrops() {
        return capturedDrops.get();
    }

    public static void clearCapturedDrops() {
        capturedDrops.get().clear();
    }

    public static void addCapturedXp(int amount) {
        capturedXp.set(capturedXp.get() + amount);
    }

    public static int getCapturedXp() {
        return capturedXp.get();
    }

    public static void clearCapturedXp() {
        capturedXp.set(0);
    }
}
