package com.xxyxxdmc;

import com.xxyxxdmc.jade.ChainMineHudRenderer;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;

@SuppressWarnings({"unused", "deprecated"})
public class HoshikimaClient implements ClientModInitializer {
	private BlockPos lastQueriedPos = null;
	private boolean lastIsChainKeyDown = false;
	private Direction lastDirection = null;
	public static int currentChainMode = 0;
	public static int totalChainBlocks = 0;
	public static int skippedAirs = 0;
	private static final HoshikimaConfig config = HoshikimaConfig.get();

	@Override
	public void onInitializeClient() {
		if (!config.enableChainMine) return;
		HoshikimaKeyBind.register();
		ChainMineOutlineRenderer.init();
		ClientPlayNetworking.registerGlobalReceiver(UpdateChainMineOutlinePacket.ID, (payload, context) -> context.client().execute(() -> {
            ChainMineOutlineRenderer.setBlocksToRender(payload.blocks());
            currentChainMode = payload.chainMode();
            totalChainBlocks = payload.blocks().size();
            skippedAirs = payload.totalSkipAirs();
        }));
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		HudRenderCallback.EVENT.register(this::onHudRender);
		HudRenderCallback.EVENT.register(ChainMineHudRenderer::render);
	}

	private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || !HoshikimaKeyBind.CHAIN_MINE_KEY.isPressed() || config.hudDisplayWay == 0) {
			return;
		}

		boolean state = totalChainBlocks == 0;

		Text line1, line2, line3, line4;

		line1 = Text.translatable("hud.hoshikima.chain.mine.state").append(": ").append(state ? Text.translatable("hud.hoshikima.chain.mine.disable").formatted(Formatting.DARK_RED) : Text.translatable("hud.hoshikima.chain.mine.enable").formatted(Formatting.GREEN));

		line2 = Text.translatable("config.hoshikima.chain.mine.chain.mode").append(": ").append(Text.translatable(getCurrentChainMode(currentChainMode)));

		line3 = Text.translatable("hud.hoshikima.chain.mine.total.chain.block").append(": ").append(String.valueOf(totalChainBlocks));

		line4 = Text.translatable("hud.hoshikima.chain.mine.skip.blocks.total").append(": ").append(String.valueOf(skippedAirs));

		int x = 0;
		int y = 0;
		int lineHeight = client.textRenderer.fontHeight;
		int backgroundColor = 0x80000000;
		int textColor = 0xFFFFFFFF;

		int width1 = client.textRenderer.getWidth(line1);
		context.fill(x, y, x + width1, y + lineHeight, backgroundColor);
		context.drawTextWithShadow(client.textRenderer, line1, x, y, textColor);

		int y2 = y + lineHeight;
		int width2 = client.textRenderer.getWidth(line2);
		context.fill(x, y2, x + width2, y2 + lineHeight, backgroundColor);
		context.drawTextWithShadow(client.textRenderer, line2, x, y2, textColor);

		int y3 = y2 + lineHeight;
		int width3 = client.textRenderer.getWidth(line3);
		if (!state) {
			context.fill(x, y3, x + width3, y3 + lineHeight, backgroundColor);
			context.drawTextWithShadow(client.textRenderer, line3, x, y3, textColor);
		}

		int y4 = y3 + lineHeight;
		int width4 = client.textRenderer.getWidth(line4);
		if (!state && skippedAirs != 0) {
			context.fill(x, y4, x + width4, y4 + lineHeight, backgroundColor);
			context.drawTextWithShadow(client.textRenderer, line4, x, y4, textColor);
		}
	}

	private String getCurrentChainMode(int mode) {
		return switch (mode) {
			case 1 -> "config.hoshikima.chain.mine.mode.two";
			default -> "config.hoshikima.chain.mine.mode.one";
		};
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		boolean isChainKeyDown = HoshikimaKeyBind.CHAIN_MINE_KEY.isPressed();
		HitResult hitResult = client.crosshairTarget;
		Direction currentDirection = null;
		BlockPos currentTargetPos = null;

		if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult) hitResult;
			currentTargetPos = blockHitResult.getBlockPos();
			currentDirection = blockHitResult.getSide();
		}

		boolean isStatusChanged = isChainKeyDown != lastIsChainKeyDown ||
				!Objects.equals(currentTargetPos, lastQueriedPos) ||
				!Objects.equals(currentDirection, lastDirection);

		if (!isStatusChanged) {
			return;
		}

		lastIsChainKeyDown = isChainKeyDown;
		lastQueriedPos = currentTargetPos;
		lastDirection = currentDirection;

		if (isChainKeyDown && currentTargetPos != null) {
			int directionIndex = currentDirection != null ? currentDirection.getIndex() : -1;
			ClientPlayNetworking.send(new QueryChainMineBlocksPacket(currentTargetPos, true, directionIndex));
		} else {
			ClientPlayNetworking.send(new QueryChainMineBlocksPacket(BlockPos.ORIGIN, false, -1));
		}
	}
}
