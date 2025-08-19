package com.xxyxxdmc;

import com.xxyxxdmc.networking.payload.ChainMineKeyPressPayload;
import com.xxyxxdmc.networking.payload.UpdateChainMineOutlinePacket;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

import com.xxyxxdmc.config.HoshikimaConfig;
import com.xxyxxdmc.key.HoshikimaKeyBind;
import com.xxyxxdmc.networking.payload.QueryChainMineBlocksPacket;
import com.xxyxxdmc.render.ChainMineOutlineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("unused")
public class HoshikimaClient implements ClientModInitializer {
	private boolean wasChainMineKeyPressed = false;
	private int direction = 0;
	private boolean directionChanged = false;
	private BlockPos lastQueriedPos = null;
	private static final HoshikimaConfig config = HoshikimaConfig.get();

	@Override
	public void onInitializeClient() {
		if (!config.enableChainMine) return;
		HoshikimaKeyBind.register();
		ChainMineOutlineRenderer.init();
		ClientPlayNetworking.registerGlobalReceiver(UpdateChainMineOutlinePacket.ID, (payload, context) -> {
			context.client().execute(() -> {
				ChainMineOutlineRenderer.setBlocksToRender(payload.blocks());
			});
		});
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		HudRenderCallback.EVENT.register(this::onHudRender);
	}

	private void onHudRender(DrawContext context, RenderTickCounter tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null) {
			return;
		}

		Text textToDraw = Text.literal("HUD Text");
		int x = 10;
		int y = 10;
		int backgroundColor = 0x80000000;
		int textColor = 0xFFFFFFFF;

		// 计算文本尺寸以绘制背景
		int textWidth = client.textRenderer.getWidth(textToDraw);
		int textHeight = client.textRenderer.fontHeight;
		int padding = 4;

		// 【第一步】：绘制背景矩形
		context.fill(
				x - padding,
				y - padding,
				x + textWidth + padding,
				y + textHeight + padding,
				backgroundColor
		);

		context.drawTextWithShadow(
				client.textRenderer,
				textToDraw,
				x,
				y,
				textColor
		);
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
