package com.vexvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class VexVisualsClient implements ClientModInitializer {
    public static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vexvisuals.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.vexvisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            Hud.tick(client);
            ModuleManager.tick(client);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> Hud.resetSession());
        HudRenderCallback.EVENT.register(Hud::render);
    }
}
