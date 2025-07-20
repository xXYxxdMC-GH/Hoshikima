package com.xxyxxdmc.init.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface ItemPickupCallback {
    Event<ItemPickupCallback> EVENT = EventFactory.createArrayBacked(ItemPickupCallback.class,
            (listeners) -> (player, itemEntity, stack) -> { // 添加 stack 参数
                for (ItemPickupCallback listener : listeners) {
                    ActionResult result = listener.onItemPickup(player, itemEntity, stack);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult onItemPickup(PlayerEntity player, ItemEntity itemEntity, ItemStack stack); // 修正方法名和参数
}
