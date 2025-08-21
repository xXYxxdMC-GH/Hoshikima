package com.xxyxxdmc;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.init.ModDataComponents;
import com.xxyxxdmc.init.ModEnchantments;
import com.xxyxxdmc.init.ModItem;
import com.xxyxxdmc.init.api.ISolidColorElementFactory;
import com.xxyxxdmc.init.callback.ItemPickupCallback;
import com.xxyxxdmc.init.recipe.ModRecipe;
import com.xxyxxdmc.networking.payload.ModMessages;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import java.util.logging.Logger;

import static com.xxyxxdmc.init.ModDataComponents.COUNT;

public class Hoshikima implements ModInitializer {
	public static final String MOD_ID = "hoshikima";
	public static final Identifier CHAIN_MINE_PACKET_ID = Identifier.of(MOD_ID, "chain_mine_key_state");
	public static ISolidColorElementFactory solidColorElementFactory;
	public static final Logger LOGGER = Logger.getLogger("Hoshikima");

	@Override
	public void onInitialize() {
		HoshikimaConfig config = HoshikimaConfig.get();
		if (config.enableEnderPearlBundle) {
			ModItem.EnderPearlBundleRegister.initialize();
			ItemPickupCallback.EVENT.register((player, itemEntity, pickedStack) -> {
				if (pickedStack.getItem() != Items.ENDER_PEARL || player.getWorld().isClient) {
					return ActionResult.PASS;
				}
				ItemStack remainingStack = pickedStack.copy();
				int initialCount = remainingStack.getCount();
				tryAbsorbPearls(remainingStack, player.getOffHandStack());
				if (!remainingStack.isEmpty()) {
					for (ItemStack bundleInInventory : player.getInventory().getMainStacks()) {
						tryAbsorbPearls(remainingStack, bundleInInventory);
						if (remainingStack.isEmpty()) {
							break;
						}
					}
				}
				int absorbedCount = initialCount - remainingStack.getCount();
				if (absorbedCount <= 0) {
					return ActionResult.PASS;
				}
				player.increaseStat(Stats.PICKED_UP.getOrCreateStat(Items.ENDER_PEARL), absorbedCount);
				player.triggerItemPickedUpByEntityCriteria(itemEntity);

				if (remainingStack.isEmpty()) {
					player.sendPickup(itemEntity, absorbedCount);
					itemEntity.discard();
					return ActionResult.SUCCESS;
				} else {
					itemEntity.setStack(remainingStack);
					return ActionResult.PASS;
				}
			});
			ModEnchantments.registerModEnchantment();
			ModDataComponents.register();
			ModRecipe.register();
		}
		if (config.enableLargeBucket) ModItem.LargeBucketRegister.initialize();
		if (config.enableFireworkThruster) ModItem.FireworkThrusterRegister.initialize();
		if (config.enableMultiFluidBucket) ModItem.MultiFluidBucketRegister.initialize();
		if (config.enableRottenFleshCluster) ModItem.RottenFleshClusterRegister.initialize();

		ModMessages.registerC2SPayloads();
		ModMessages.registerS2CPayloads();

		// TODO: Add Data pack in mod.
		// var modContainer = FabricLoader.getInstance()
		// 		 .getModContainer(MOD_ID)
		// 		 .orElseThrow(() -> new IllegalStateException("Could not find mod container for " + MOD_ID));
		// ResourceManagerHelper.registerBuiltinResourcePack(
		// 		 Identifier.of(MOD_ID, "hoshikima_raw_ore_recipe_data"),
		// 		 modContainer,
		// 		 Text.translatable("datapack.hoshikima.name"),
		// 		 ResourcePackActivationType.DEFAULT_ENABLED
		// );
	}
	private void tryAbsorbPearls(ItemStack remainingStack, ItemStack bundleStack) {
		if (remainingStack.isEmpty() || bundleStack.getItem() != ModItem.EnderPearlBundleRegister.ENDER_PEARL_BUNDLE) {
			return;
		}
		int currentPearlsInBundle = bundleStack.getOrDefault(COUNT, 0);
		int spaceInBundle = 1728 - currentPearlsInBundle;

		if (spaceInBundle <= 0) {
			return;
		}

		int pearlsToAdd = Math.min(remainingStack.getCount(), spaceInBundle);

		if (pearlsToAdd > 0) {
			bundleStack.set(COUNT, currentPearlsInBundle + pearlsToAdd);
			remainingStack.decrement(pearlsToAdd);
		}
	}
}
