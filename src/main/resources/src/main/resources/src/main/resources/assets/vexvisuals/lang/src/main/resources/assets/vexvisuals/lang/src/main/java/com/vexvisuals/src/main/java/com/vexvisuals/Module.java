package com.vexvisuals;

import net.minecraft.client.MinecraftClient;

public class Module {
    public final String id;
    public final String name;
    public final String description;
    public final Category category;
    public final boolean hasScale;
    public boolean enabled;
    public int color = 0xC6F432;
    public float scale = 1.0f;

    public Module(String id, String name, String description, Category category,
                  boolean enabledByDefault, boolean hasScale) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabledByDefault;
        this.hasScale = hasScale;
    }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    public void onEnable() {}

    public void onDisable() {}

    public void onTick(MinecraftClient mc) {}
}
