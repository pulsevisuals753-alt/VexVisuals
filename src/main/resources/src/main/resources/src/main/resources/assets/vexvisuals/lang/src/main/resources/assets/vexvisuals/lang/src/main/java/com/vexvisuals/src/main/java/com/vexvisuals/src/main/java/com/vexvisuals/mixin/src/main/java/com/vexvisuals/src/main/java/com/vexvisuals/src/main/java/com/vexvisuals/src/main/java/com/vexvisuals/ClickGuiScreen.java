package com.vexvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final int[] SWATCHES = {0xC6F432, 0x8B6CFF, 0xF3EFFA, 0xFF8A3D};
    private static final int BG = 0xFF120E1A;
    private static final int CARD = 0xFF17121F;
    private static final int LINE = 0xFF2A2238;
    private static final int MUTED = 0xFFA79FB8;
    private static final int ACCENT = 0xFFC6F432;
    private static final int DARK = 0xFF0E0B14;
    private static final int WHITE = 0xFFF3EFFA;
    private static final int CARD_H = 24, GAP = 4, TOP = 36, RAIL = 54, SIDE = 112;

    private Category cat = Category.HUD;
    private Module selected;
    private int scroll;
    private boolean sliding;

    private int px, py, pw, ph, cx, cw, sx, cols, cardW;

    public ClickGuiScreen() {
        super(Text.literal("VexVisuals"));
        selected = firstOf(cat);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        ModuleManager.save();
    }

    private Module firstOf(Category c) {
        for (Module m : ModuleManager.ALL) if (m.category == c) return m;
        return null;
    }

    private List<Module> modules() {
        List<Module> list = new ArrayList<>();
        for (Module m : ModuleManager.ALL) if (m.category == cat) list.add(m);
        return list;
    }

    private void layout() {
        pw = Math.min(width - 12, 400);
        ph = Math.min(height - 12, 230);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        cx = px + RAIL;
        sx = px + pw - SIDE;
        cw = sx - cx;
        cols = cw >= 190 ? 2 : 1;
        cardW = (cw - 20 - (cols - 1) * 6) / cols;
    }

    private int cardX(int i) {
        return cx + 10 + (i % cols) * (cardW + 6);
    }

    private int cardY(int i) {
        return py + TOP + (i / cols) * (CARD_H + GAP) - scroll;
    }

    private int maxScroll() {
        int rows = (modules().size() + cols - 1) / cols;
        return Math.max(0, rows * (CARD_H + GAP) - (ph - TOP - 8));
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        layout();
        scroll = Math.max(0, Math.min(scroll, maxScroll()));

        ctx.fill(0, 0, width, height, 0xAA0E0B14);
        ctx.fill(px, py, px + pw, py + ph, BG);
        border(ctx, px, py, pw, ph, LINE);

        // rail
        ctx.fill(px + 1, py + 1, px + RAIL, py + ph - 1, DARK);
        ctx.fill(px + RAIL / 2 - 9, py + 8, px + RAIL / 2 + 9, py + 26, ACCENT);
        ctx.drawText(textRenderer, "V", px + RAIL / 2 - 3, py + 13, DARK, false);
        Category[] cs = Category.values();
        for (int i = 0; i < cs.length; i++) {
            int bx = px + 4, by = py + 34 + i * 30, bw = RAIL - 8, bh = 26;
            boolean active = cs[i] == cat;
            if (active) ctx.fill(bx, by, bx + bw, by + bh, CARD);
            String label = cs[i].label;
            ctx.drawText(textRenderer, label, bx + (bw - textRenderer.getWidth(label)) / 2, by + 9,
                    active ? ACCENT : MUTED, false);
        }

        // header
        ctx.drawText(textRenderer, cat.label, cx + 10, py + 12, WHITE, true);

        // cards
        ctx.enableScissor(cx, py + 30, cx + cw, py + ph - 2);
        List<Module> list = modules();
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int x = cardX(i), y = cardY(i);
            ctx.fill(x, y, x + cardW, y + CARD_H, CARD);
            border(ctx, x, y, cardW, CARD_H, m == selected ? ACCENT : LINE);
            ctx.drawText(textRenderer, m.name, x + 8, y + 8, WHITE, false);
            int pl = x + cardW - 30, pt = y + 7;
            ctx.fill(pl, pt, pl + 22, pt + 10, m.enabled ? ACCENT : LINE);
            int kx = m.enabled ? pl + 14 : pl + 2;
            ctx.fill(kx, pt + 2, kx + 6, pt + 8, m.enabled ? DARK : MUTED);
        }
        ctx.disableScissor();

        // settings
        ctx.fill(sx, py + 1, px + pw - 1, py + ph - 1, CARD);
        ctx.fill(sx, py + 1, sx + 1, py + ph - 1, LINE);
        if (selected != null) {
            ctx.drawText(textRenderer, "НАСТРОЙКИ", sx + 8, py + 10, MUTED, false);
            ctx.drawText(textRenderer, selected.name, sx + 8, py + 22, WHITE, true);
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(selected.description), SIDE - 16);
            int ly = py + 36;
            for (int i = 0; i < lines.size() && i < 3; i++) {
                ctx.drawText(textRenderer, lines.get(i), sx + 8, ly, MUTED, false);
                ly += 10;
            }
            ctx.drawText(textRenderer, "Цвет", sx + 8, py + 82, MUTED, false);
            for (int i = 0; i < SWATCHES.length; i++) {
                int x = sx + 8 + i * 22, y = py + 94;
                ctx.fill(x, y, x + 18, y + 18, 0xFF000000 | SWATCHES[i]);
                if (selected.color == SWATCHES[i]) border(ctx, x - 1, y - 1, 20, 20, WHITE);
            }
            if (selected.hasScale) {
                ctx.drawText(textRenderer, "Размер: " + Math.round(selected.scale * 100) + "%", sx + 8, py + 122, MUTED, false);
                int tx = sx + 8, tw = SIDE - 16, ty = py + 140;
                float t = (selected.scale - 0.6f) / 1.4f;
                ctx.fill(tx, ty, tx + tw, ty + 4, LINE);
                ctx.fill(tx, ty, tx + Math.round(tw * t), ty + 4, ACCENT);
                int kx = tx + Math.round(tw * t) - 3;
                ctx.fill(kx, ty - 3, kx + 6, ty + 7, WHITE);
            }
        }
        ctx.drawText(textRenderer, "RShift: закрыть", sx + 8, py + ph - 14, MUTED, false);
    }

    private void slide(double mx) {
        float t = (float) ((mx - (sx + 8)) / (SIDE - 16));
        t = Math.max(0f, Math.min(1f, t));
        selected.scale = Math.round((0.6f + t * 1.4f) * 20f) / 20f;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        layout();
        Category[] cs = Category.values();
        for (int i = 0; i < cs.length; i++) {
            if (hit(mx, my, px + 4, py + 34 + i * 30, RAIL - 8, 26)) {
                cat = cs[i];
                scroll = 0;
                selected = firstOf(cat);
                return true;
            }
        }
        if (mx >= cx && mx < sx && my >= py + 30 && my < py + ph) {
            List<Module> list = modules();
            for (int i = 0; i < list.size(); i++) {
                int x = cardX(i), y = cardY(i);
                if (hit(mx, my, x, y, cardW, CARD_H)) {
                    Module m = list.get(i);
                    selected = m;
                    if (mx >= x + cardW - 34) m.setEnabled(!m.enabled);
                    return true;
                }
            }
        }
        if (selected != null) {
            for (int i = 0; i < SWATCHES.length; i++) {
                if (hit(mx, my, sx + 8 + i * 22, py + 94, 18, 18)) {
                    selected.color = SWATCHES[i];
                    return true;
                }
            }
            if (selected.hasScale && hit(mx, my, sx + 4, py + 134, SIDE - 8, 18)) {
                sliding = true;
                slide(mx);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (sliding && selected != null) {
            slide(mx);
            return true;
        }
        if (mx >= cx && mx < sx) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) dy));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        sliding = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) (v * 12)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    }
