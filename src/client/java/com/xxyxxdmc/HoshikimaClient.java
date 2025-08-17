package com.xxyxxdmc;

import com.xxyxxdmc.networking.payload.ChainMineKeyPressPayload;
import com.xxyxxdmc.networking.payload.UpdateChainMineOutlinePacket;
import net.minecraft.client.MinecraftClient;
import com.xxyxxdmc.key.HoshikimaKeyBind;
import com.xxyxxdmc.networking.payload.QueryChainMineBlocksPacket;
import com.xxyxxdmc.render.ChainMineOutlineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@SuppressWarnings("unused")
public class HoshikimaClient implements ClientModInitializer {
	private boolean wasChainMineKeyPressed = false;
	private int direction = 0;
	private boolean directionChanged = false;
	private BlockPos lastQueriedPos = null;

	@Override
	public void onInitializeClient() {
		HoshikimaKeyBind.register();
		ChainMineOutlineRenderer.init();
		ClientPlayNetworking.registerGlobalReceiver(UpdateChainMineOutlinePacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				ChainMineOutlineRenderer.setBlocksToRender(payload.blocks());
			});
		});

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}
		boolean isChainKeyDown = HoshikimaKeyBind.CHAIN_MINE_KEY.isPressed();
		HitResult hitResult = client.crosshairTarget;  
		int newDirection = 0;
		if (hitResult instanceof BlockHitResult blockHitResult) newDirection = blockHitResult.getSide().getIndex();
		if (isChainKeyDown != wasChainMineKeyPressed || direction != newDirection) {
			ClientPlayNetworking.send(new ChainMineKeyPressPayload(isChainKeyDown, newDirection));
			wasChainMineKeyPressed = isChainKeyDown;
			direction = newDirection;
			directionChanged = true;
		}
		BlockPos targetPos = null;
		if (isChainKeyDown && hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
			targetPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
		}
		if (targetPos == null) {
			if (lastQueriedPos != null) {
				ChainMineOutlineRenderer.clear();
				lastQueriedPos = null;
			}
			return;
		}

		if (directionChanged || targetPos.equals(lastQueriedPos)){
			lastQueriedPos = targetPos;
			ClientPlayNetworking.send(new QueryChainMineBlocksPacket(targetPos));
		}
	}
}
