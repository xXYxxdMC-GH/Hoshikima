package com.xxyxxdmc.key;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HoshikimaKeyBind {
    public static KeyBinding CHAIN_MINE_KEY;

    public static void register() {
        CHAIN_MINE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hoshikima.chain_mine",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.category.hoshikima"
        ));
    }
}
