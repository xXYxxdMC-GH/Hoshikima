package com.xxyxxdmc;

import com.xxyxxdmc.key.HoshikimaKeyBind;
import com.xxyxxdmc.networking.payload.ChainMineKeyPressPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@SuppressWarnings("unused")
public class HoshikimaClient implements ClientModInitializer {
	private boolean wasChainMineKeyPressed = false;
	@Override
	public void onInitializeClient() {
		HoshikimaKeyBind.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}

			boolean isChainMineKeyPressed = HoshikimaKeyBind.CHAIN_MINE_KEY.isPressed();

			if (isChainMineKeyPressed != wasChainMineKeyPressed) {
				wasChainMineKeyPressed = isChainMineKeyPressed;

				ChainMineKeyPressPayload payload = new ChainMineKeyPressPayload(isChainMineKeyPressed);

				ClientPlayNetworking.send(payload);
			}
		});
	}
}