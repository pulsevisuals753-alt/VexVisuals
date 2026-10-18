package com.vexvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Locale;

public final class Hud {
    private static final ArrayDeque<Long> leftClicks = new ArrayDeque<>();
    private static final ArrayDeque<Long> rightClicks = new ArrayDeque<>();
    private static boolean lastLeft, lastRight;
    private static double speed;
    private static long sessionStart = System.currentTimeMillis();

    private Hud() {}

    public static void resetSession() {
        sessionStart = System.currentTimeMillis();
    }

    public static void tick(MinecraftClient mc) {
        long now = System.currentTimeMillis();
        long handle = mc.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !lastLeft) leftClicks.add(now);
        if (r && !lastRight) rightClicks.add(now);
        lastLeft = l;
        lastRight = r;
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000) rightClicks.pollFirst();

        ClientPlayerEntity p = mc.player;
        if (p != null) {
            double dx = p.getX() - p.prevX;
            double dz = p.getZ() - p.prevZ;
            speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
        }
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;

        int y = 6;
        y = line(ctx, tr, ModuleManager.FPS, "FPS: " + mc.getCurrentFps(), y);
        y = line(ctx, tr, ModuleManager.COORDS, String.format(Locale.ROOT, "XYZ: %.1f %.1f %.1f", p.getX(), p.getY(), p.getZ()), y);
        y = line(ctx, tr, ModuleManager.DIRECTION, "Смотришь: " + direction(p), y);
        y = line(ctx, tr, ModuleManager.PING, "Пинг: " + ping(mc, p) + " мс", y);
        y = line(ctx, tr, ModuleManager.CPS, "CPS: " + leftClicks.size() + " | " + rightClicks.size(), y);
        y = line(ctx, tr, ModuleManager.SPEED, String.format(Locale.ROOT, "Скорость: %.1f б/с", speed), y);
        long sec = (System.currentTimeMillis() - sessionStart) / 1000;
        y = line(ctx, tr, ModuleManager.SESSION, String.format(Locale.ROOT, "Сессия: %02d:%02d", sec / 60, sec % 60), y);

        keystrokes(ctx, tr, mc);
        armor(ctx, tr, mc, p);
    }

    private static String direction(ClientPlayerEntity p) {
        return switch (p.getHorizontalFacing()) {
            case NORTH -> "Север";
            case SOUTH -> "Юг";
            case WEST -> "Запад";
            case EAST -> "Восток";
            default -> "?";
        };
    }

    private static int ping(MinecraftClient mc, ClientPlayerEntity p) {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
        return e == null ? 0 : e.getLatency();
    }

    private static int line(DrawContext ctx, TextRenderer tr, Module m, String text, int y) {
        if (!m.enabled) return y;
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(6, y, 0);
        ms.scale(m.scale, m.scale, 1.0f);
        ctx.drawText(tr, text, 0, 0, 0xFF000000 | m.color, true);
        ms.pop();
        return y + Math.round(11 * m.scale);
    }

    private static void keystrokes(DrawContext ctx, TextRenderer tr, MinecraftClient mc) {
        Module m = ModuleManager.KEYS;
        if (!m.enabled) return;
        float s = m.scale;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(sw - 70 * s, sh / 2.0 - 34 * s, 0);
        ms.scale(s, s, 1.0f);
        box(ctx, tr, "W", 22, 0, 20, 20, mc.options.forwardKey.isPressed(), m.color);
        box(ctx, tr, "A", 0, 22, 20, 20, mc.options.leftKey.isPressed(), m.color);
        box(ctx, tr, "S", 22, 22, 20, 20, mc.options.backKey.isPressed(), m.color);
        box(ctx, tr, "D", 44, 22, 20, 20, mc.options.rightKey.isPressed(), m.color);
        box(ctx, tr, "ЛКМ", 0, 44, 31, 20, mc.options.attackKey.isPressed(), m.color);
        box(ctx, tr, "ПКМ", 33, 44, 31, 20, mc.options.useKey.isPressed(), m.color);
        ms.pop();
    }

    private static void box(DrawContext ctx, TextRenderer tr, String label, int x, int y, int w, int h,
                            boolean pressed, int color) {
        ctx.fill(x, y, x + w, y + h, pressed ? (0xCC000000 | color) : 0x88000000);
        int tc = pressed ? 0xFF0E0B14 : 0xFFFFFFFF;
        ctx.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2, tc, false);
    }

    private static void armor(DrawContext ctx, TextRenderer tr, MinecraftClient mc, ClientPlayerEntity p) {
        Module m = ModuleManager.ARMOR;
        if (!m.enabled) return;
        float s = m.scale;
        int sh = ctx.getScaledWindowHeight();
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(6, sh - 100 * s, 0);
        ms.scale(s, s, 1.0f);
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = p.getEquippedStack(slots[i]);
            if (stack.isEmpty()) continue;
            ctx.drawItem(stack, 0, i * 18);
            ctx.drawStackOverlay(tr, stack, 0, i * 18);
        }
        ms.pop();
    }
    }
