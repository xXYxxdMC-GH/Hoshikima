package com.xxyxxdmc.mixin.client;

import com.xxyxxdmc.key.HoshikimaKeyBind;
import com.xxyxxdmc.networking.payload.ChangeChainModePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void hoshikima$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null && client.player != null &&
                HoshikimaKeyBind.CHAIN_MINE_KEY.isPressed() &&
                InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)) {
            ClientPlayNetworking.send(new ChangeChainModePayload(vertical > 0));
            ci.cancel();
        }
    }
}
