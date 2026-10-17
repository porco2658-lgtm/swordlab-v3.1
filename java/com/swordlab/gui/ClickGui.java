package com.swordlab.gui;

import com.swordlab.module.Category;
import com.swordlab.module.Keys;
import com.swordlab.module.Module;
import com.swordlab.module.ModuleManager;
import com.swordlab.module.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Dark purple click GUI: sidebar categories, header + search, rounded module cards with switches. */
public class ClickGui extends Screen {
    private static final int SIDE = 104, R = 12, CARD_H = 38;

    // palette
    private static final int BG_TOP = 0xF2110C28, BG_BOT = 0xF20A0818, SIDE_TINT = 0x55000000, LINE = 0xFF2A2252;
    private static final int CARD = 0xFF14102E, CARD_HOVER = 0xFF1B1640, INNER = 0xFF0F0B24;
    private static final int ACCENT = 0xFF8B5CF6, ACCENT_D = 0xFF5B35B8, SEL_TOP = 0xFF4A2A9B, SEL_BOT = 0xFF5B35B8;
    private static final int TEXT = 0xFFF2F0FF, DIM = 0xFF9A93C4, FAINT = 0xFF6B648F, TRACK = 0xFF2A2552;

    private enum Kind { TAB, TOGGLE, ARROW, BOOL, SLIDER, KEY, MODE }

    private record Hit(int x1, int y1, int x2, int y2, Kind kind, Object ref) {
        boolean in(double x, double y) { return x >= x1 && x < x2 && y >= y1 && y < y2; }
    }

    private final List<Hit> hits = new ArrayList<>();
    private Category category = Category.SWORD;
    private Hit dragging;
    private Setting listening; // key setting waiting for a key press
    private EditBox search;
    private float scroll;
    private int contentH, cy1, cy2;

    public ClickGui() { super(Component.literal("SwordLab V2")); }

    private int pw() { return Math.min(400, width - 10); }
    private int ph() { return Math.min(250, height - 10); }

    @Override public boolean isPauseScreen() { return false; }

    // No vanilla dim/blur.
    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}

    @Override
    protected void init() {
        int px = (width - pw()) / 2, py = (height - ph()) / 2;
        int sx2 = px + pw() - 14, sx1 = sx2 - 112, sy1 = py + 14;
        String old = search == null ? "" : search.getValue();
        search = new EditBox(font, sx1 + 10, sy1 + 4, 112 - 20, 12, Component.literal("Search"));
        search.setBordered(false);
        search.setMaxLength(24);
        search.setTextColor(TEXT);
        search.setHint(Component.literal("Search modules..."));
        search.setValue(old);
        addRenderableWidget(search);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        hits.clear();
        int pw = pw(), ph = ph();
        int px = (width - pw) / 2, py = (height - ph) / 2;

        // Window
        Gfx.rr(g, px, py, px + pw, py + ph, R, BG_TOP, BG_BOT);
        g.fill(px + R, py + 1, px + SIDE, py + ph - 1, SIDE_TINT);
        g.fill(px + 1, py + R, px + R, py + ph - R, SIDE_TINT);
        Gfx.border(g, px, py, px + pw, py + ph, R, LINE);
        g.fill(px + SIDE, py + 12, px + SIDE + 1, py + ph - 12, LINE);

        // Brand
        Gfx.rr(g, px + 14, py + 14, px + 32, py + 32, 6, ACCENT, ACCENT_D);
        t(g, UiFont.b("S"), px + 23 - font.width(UiFont.b("S")) / 2, py + 19, 0xFFFFFFFF);
        t(g, UiFont.b("SwordLab V2"), px + 38, py + 15, TEXT);
        t(g, UiFont.c("TEST CLIENT"), px + 38, py + 25, FAINT);

        // Categories
        t(g, UiFont.b("MODULES"), px + 14, py + 46, FAINT);
        int ty = py + 60;
        for (Category c : Category.values()) {
            int x1 = px + 8, x2 = px + SIDE - 8;
            boolean sel = c == category && search.getValue().isBlank();
            if (sel) {
                Gfx.rr(g, x1, ty, x2, ty + 24, 8, SEL_TOP, SEL_BOT);
                Gfx.border(g, x1, ty, x2, ty + 24, 8, 0xFF7C5CE0);
            }
            Gfx.rr(g, x1 + 6, ty + 5, x1 + 20, ty + 19, 4, sel ? 0x40FFFFFF : 0xFF1B1640);
            String ini = c.label.substring(0, 1);
            t(g, UiFont.b(ini), x1 + 13 - font.width(UiFont.b(ini)) / 2, ty + 8, sel ? 0xFFFFFFFF : DIM);
            t(g, UiFont.b(c.label), x1 + 26, ty + 8, sel ? 0xFFFFFFFF : DIM);
            String n = String.valueOf(ModuleManager.in(c).size());
            t(g, UiFont.c(n), x2 - 8 - font.width(UiFont.c(n)), ty + 8, sel ? 0xCCFFFFFF : FAINT);
            hits.add(new Hit(x1, ty, x2, ty + 24, Kind.TAB, c));
            ty += 28;
        }
        t(g, UiFont.c("Right-click: settings"), px + 14, py + ph - 30, FAINT);
        t(g, UiFont.c("Right Shift: close"), px + 14, py + ph - 20, FAINT);

        // Module list (search shows matches from every category)
        String q = search.getValue().trim().toLowerCase();
        List<Module> mods = q.isEmpty()
                ? ModuleManager.in(category)
                : ModuleManager.ALL.stream().filter(m -> m.name.toLowerCase().contains(q)).toList();
        long on = mods.stream().filter(m -> m.enabled).count();

        int cx1 = px + SIDE + 14, cx2 = px + pw - 14;
        Component title = q.isEmpty() ? UiFont.bi(category.label) : UiFont.bi("Search");
        t(g, title, cx1, py + 14, ACCENT);
        t(g, UiFont.b(" Modules"), cx1 + font.width(title), py + 14, TEXT);
        t(g, UiFont.c(mods.size() + " modules  |  " + on + " enabled"), cx1, py + 28, DIM);

        // Search box chrome (the EditBox itself is drawn last by super.render)
        int sx2 = px + pw - 14, sx1 = sx2 - 112, sy1 = py + 14;
        Gfx.rr(g, sx1, sy1, sx2, sy1 + 20, 8, CARD);
        Gfx.border(g, sx1, sy1, sx2, sy1 + 20, 8, LINE);

        cy1 = py + 48;
        cy2 = py + ph - 12;
        int viewH = cy2 - cy1;
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentH - viewH)));

        g.enableScissor(cx1 - 2, cy1, cx2 + 2, cy2);
        int y = cy1 - (int) scroll, total = 0;
        int cardX2 = cx2 - 8;
        for (Module m : mods) {
            int panelH = m.expanded ? m.settings.size() * 24 + 8 : 0;
            int h = CARD_H + (m.expanded ? panelH + 8 : 0);
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.35f;
            boolean hover = mx >= cx1 && mx < cardX2 && my >= Math.max(y, cy1) && my < Math.min(y + CARD_H, cy2);

            Gfx.rr(g, cx1, y, cardX2, y + h, 9, hover ? CARD_HOVER : CARD);
            Gfx.border(g, cx1, y, cardX2, y + h, 9, LINE);
            t(g, UiFont.b(m.name), cx1 + 12, y + 7, TEXT);
            t(g, UiFont.c(fit(m.description, cardX2 - cx1 - 12 - 66)), cx1 + 12, y + 20, DIM);

            int pillX = cardX2 - 12 - 26;
            pill(g, pillX, y + 12, m.anim);
            Component chev = UiFont.b(m.expanded ? "v" : ">");
            t(g, chev, pillX - 16, y + 14, DIM);
            hits.add(new Hit(cx1, y, cardX2, y + CARD_H, Kind.TOGGLE, m));
            hits.add(new Hit(pillX - 26, y, pillX - 4, y + CARD_H, Kind.ARROW, m));

            if (m.expanded) {
                int sp = y + CARD_H;
                Gfx.rr(g, cx1 + 8, sp, cardX2 - 8, sp + panelH, 7, INNER);
                int sy = sp + 4;
                for (Setting s : m.settings) {
                    drawSetting(g, s, cx1 + 18, cardX2 - 18, sy);
                    sy += 24;
                }
            }
            y += h + 5;
            total += h + 5;
        }
        g.disableScissor();
        contentH = total;

        // Scroll thumb
        if (contentH > viewH) {
            int th = Math.max(18, viewH * viewH / contentH);
            float prog = scroll / (contentH - viewH);
            int thy = cy1 + Math.round((viewH - th) * prog);
            Gfx.rr(g, px + pw - 9, thy, px + pw - 6, thy + th, 1, 0x99_8B5CF6);
        }

        super.render(g, mx, my, pt); // draws the search EditBox
    }

    private void drawSetting(GuiGraphics g, Setting s, int x1, int x2, int sy) {
        switch (s.type) {
            case BOOL -> {
                s.anim += ((s.bool ? 1f : 0f) - s.anim) * 0.35f;
                t(g, UiFont.c(s.name), x1, sy + 8, DIM);
                pill(g, x2 - 26, sy + 5, s.anim);
                hits.add(new Hit(x1 - 8, sy, x2 + 8, sy + 24, Kind.BOOL, s));
            }
            case NUMBER -> {
                t(g, UiFont.c(s.name), x1, sy + 1, DIM);
                String v = s.step >= 1 ? String.valueOf(s.asInt())
                        : String.format(s.step >= 0.1 ? "%.1f" : "%.2f", s.value);
                t(g, UiFont.b(v), x2 - font.width(UiFont.b(v)), sy + 1, TEXT);
                int ty2 = sy + 15;
                float f = (float) ((s.value - s.min) / (s.max - s.min));
                Gfx.rr(g, x1, ty2, x2, ty2 + 4, 2, TRACK);
                int fx = x1 + (int) ((x2 - x1) * f);
                if (fx - x1 > 1) Gfx.rr(g, x1, ty2, fx, ty2 + 4, 2, ACCENT);
                Gfx.rr(g, fx - 3, ty2 - 3, fx + 3, ty2 + 7, 3, 0xFFFFFFFF);
                hits.add(new Hit(x1, sy + 10, x2, sy + 23, Kind.SLIDER, s));
            }
            case KEY -> {
                boolean wait = listening == s;
                t(g, UiFont.c(s.name), x1, sy + 8, DIM);
                box(g, x2 - 84, sy + 4, x2, sy + 20, wait ? "press a key..." : Keys.name(s.key), wait);
                hits.add(new Hit(x2 - 84, sy + 4, x2, sy + 20, Kind.KEY, s));
            }
            case MODE -> {
                t(g, UiFont.c(s.name), x1, sy + 8, DIM);
                box(g, x2 - 84, sy + 4, x2, sy + 20, s.modes[s.mode], false);
                hits.add(new Hit(x2 - 84, sy + 4, x2, sy + 20, Kind.MODE, s));
            }
        }
    }

    private void box(GuiGraphics g, int x1, int y1, int x2, int y2, String label, boolean active) {
        Gfx.rr(g, x1, y1, x2, y2, 6, active ? ACCENT_D : CARD_HOVER);
        Gfx.border(g, x1, y1, x2, y2, 6, active ? ACCENT : LINE);
        Component c = UiFont.b(label);
        t(g, c, x1 + (x2 - x1 - font.width(c)) / 2, y1 + 4, TEXT);
    }

    private void pill(GuiGraphics g, int x, int y, float a) {
        Gfx.rr(g, x, y, x + 26, y + 14, 7, Gfx.lerp(TRACK, ACCENT, a));
        int kx = x + 2 + Math.round(12 * a);
        Gfx.rr(g, kx, y + 2, kx + 10, y + 12, 5, 0xFFFFFFFF);
    }

    private void t(GuiGraphics g, Component c, int x, int y, int color) {
        g.drawString(font, c, x, y, color, false);
    }

    /** Cuts the text with "..." so it fits in maxW pixels. */
    private String fit(String s, int maxW) {
        if (font.width(UiFont.c(s)) <= maxW) return s;
        while (s.length() > 1 && font.width(UiFont.c(s + "...")) > maxW) s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    private void setSlider(Hit h, double mx) {
        Setting s = (Setting) h.ref();
        double t = Math.max(0, Math.min(1, (mx - h.x1()) / (double) (h.x2() - h.x1())));
        s.set(s.min + t * (s.max - s.min));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x(), my = e.y();
        int b = e.button();
        listening = null;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.in(mx, my)) continue;
            if (h.kind() != Kind.TAB && (my < cy1 || my >= cy2)) continue;
            search.setFocused(false);
            switch (h.kind()) {
                case TAB -> { category = (Category) h.ref(); scroll = 0; search.setValue(""); }
                case ARROW -> ((Module) h.ref()).expanded ^= true;
                case TOGGLE -> {
                    Module m = (Module) h.ref();
                    if (b == 1) m.expanded ^= true; else m.toggle();
                }
                case BOOL -> ((Setting) h.ref()).bool ^= true;
                case SLIDER -> { dragging = h; setSlider(h, mx); }
                case KEY -> listening = (Setting) h.ref();
                case MODE -> ((Setting) h.ref()).cycle();
            }
            return true;
        }
        return super.mouseClicked(e, dbl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragging != null) { setSlider(dragging, e.x()); return true; }
        return super.mouseDragged(e, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        dragging = null;
        return super.mouseReleased(e);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll -= (float) sy * 16f;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        if (listening != null) {
            int k = e.key();
            if (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) listening.key = -1;
            else if (k != GLFW.GLFW_KEY_ESCAPE) listening.key = k;
            listening = null;
            return true;
        }
        if (!search.isFocused() && e.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) { onClose(); return true; }
        return super.keyPressed(e);
    }
}
