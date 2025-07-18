package com.xxyxxdmc;

import com.xxyxxdmc.init.ItemPickupCallback;
import com.xxyxxdmc.init.ModItem;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;

public class RandomThing implements ModInitializer {
	public static final String MOD_ID = "randomthing";

	@Override
	public void onInitialize() {
		ModItem.initialize();
		ItemPickupCallback.EVENT.register(((player, itemEntity, pickedStack) -> {
			if (!player.getInventory().contains(new ItemStack(ModItem.ENDER_PEARL_BUNDLE))) return ActionResult.PASS;
			if (pickedStack.getItem() == Items.ENDER_PEARL) {
				if (player.getWorld().isClient) {
					return ActionResult.PASS;
				}
				ItemStack remainingStack = pickedStack.copy();

				for (int i = 0; i < player.getInventory().size(); i++) {
					ItemStack bundleStack = player.getInventory().getStack(i);
					if (bundleStack.getItem() == ModItem.ENDER_PEARL_BUNDLE) {
						int currentPearlsInBundle = bundleStack.getMaxDamage() - bundleStack.getDamage();
						int bundleCapacity = bundleStack.getMaxDamage();
						int spaceInBundle = bundleCapacity - currentPearlsInBundle;

						if (spaceInBundle > 0) {
							int pearlsToAdd = Math.min(remainingStack.getCount(), spaceInBundle);

							bundleStack.setDamage(bundleStack.getDamage() - pearlsToAdd);

							remainingStack.decrement(pearlsToAdd);

							if (remainingStack.isEmpty()) {
								player.increaseStat(Stats.PICKED_UP.getOrCreateStat(Items.ENDER_PEARL), pickedStack.getCount());
								player.triggerItemPickedUpByEntityCriteria(itemEntity);

								return ActionResult.SUCCESS;
							}
						}
					}
				}

				if (!remainingStack.isEmpty()) {
					boolean insertedIntoInventory = player.getInventory().insertStack(remainingStack);

					if (insertedIntoInventory && remainingStack.isEmpty()) {
						player.increaseStat(Stats.PICKED_UP.getOrCreateStat(Items.ENDER_PEARL), pickedStack.getCount());
						player.triggerItemPickedUpByEntityCriteria(itemEntity);
						return ActionResult.SUCCESS;
					} else if (insertedIntoInventory) {
						return ActionResult.PASS;
					}
				}

				return ActionResult.PASS;
			}
			return ActionResult.PASS;
		}));
	}
}