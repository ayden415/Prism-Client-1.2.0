package dev.prism.core;

import dev.prism.core.Feature.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The whole menu: layout, animation, hit testing and input. It never touches Minecraft classes;
 * the per-version MenuScreen just forwards render + input events into this class.
 */
public final class MenuUI {
    /** Called once the close animation has finished; the Screen should really close itself here. */
    public interface Host {
        void finishClose();
    }

    private enum Type { TAB, SEARCH, HEADER, SWITCH, BOOL, MODE, SLIDER }

    private record K(Object o, String k) {}

    private record Hit(Type type, int x, int y, int w, int h, Object ref, boolean content) {
        boolean contains(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private static final int SIDEBAR_W = 108, HEADER_H = 32, CARD_GAP = 6, TAB_H = 22;

    private final Host host;
    private final Supplier<String> openKeyName;
    private final Fade fade = new Fade();
    private final Smooth open = new Smooth(0f);
    private final Smooth tabY = new Smooth(0f);
    private final Smooth scroll = new Smooth(0f);
    private final Map<K, Smooth> anims = new HashMap<>();
    private final Set<Feature> expanded = new HashSet<>();
    private final List<Hit> hits = new ArrayList<>();

    private Category tab = Category.HUD;
    private boolean tabInit, closing, searchFocus;
    private String search = "";
    private float scrollTarget;
    private Hit dragging;
    private long last = System.nanoTime();

    // layout remembered from the last frame (for input)
    private int panelX, panelY, panelW, panelH, listTop, listBottom, contentH;

    public MenuUI(Host host, Supplier<String> openKeyName) {
        this.host = host;
        this.openKeyName = openKeyName;
    }

    // =====================================================================================
    // Rendering
    // =====================================================================================

    public void render(Draw base, int mx, int my, int sw, int sh) {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - last) / 1e9f);
        last = now;

        float o = open.update(closing ? 0f : 1f, dt, 16f);
        if (closing && o < 0.02f) {
            host.finishClose();
            return;
        }
        hits.clear();
        Draw d = fade.with(base, o);
        int accent = Theme.accent();

        // dim backdrop
        int dim = (int) (Features.DIM.value / 100.0 * 255.0);
        d.fill(0, 0, sw, sh, dim << 24);

        // panel
        panelW = Math.min(450, sw - 16);
        panelH = Math.min(290, sh - 16);
        panelX = (sw - panelW) / 2;
        panelY = (sh - panelH) / 2 + Math.round((1f - o) * 10f);
        int px = panelX, py = panelY, pw = panelW, ph = panelH;

        d.shadow(px, py, pw, ph, 6, 8, 0x0A);
        d.card(px, py, pw, ph, 6, Theme.PANEL, Theme.BORDER);

        drawSidebar(d, px, py, ph, mx, my, dt, accent);
        drawContent(d, px, py, pw, ph, mx, my, dt, accent);
    }

    private void drawSidebar(Draw d, int px, int py, int ph, int mx, int my, float dt, int accent) {
        int sb = SIDEBAR_W;
        d.roundRect(px + 1, py + 1, sb, ph - 2, 5, Theme.SIDEBAR);
        d.fill(px + sb - 5, py + 1, px + sb, py + ph - 1, Theme.SIDEBAR); // square off the right edge
        d.fill(px + sb, py + 1, px + sb + 1, py + ph - 1, Theme.BORDER);

        // brand
        d.text("PRISM", px + 14, py + 13, accent, true);
        d.text("client", px + 14, py + 24, Theme.TEXT_DIM, false);
        d.roundRect(px + 14, py + 37, 18, 2, 1, accent);

        // tabs
        int tabsTop = py + 50;
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            Category c = cats[i];
            int ty = tabsTop + i * (TAB_H + 2);
            if (c == tab) {
                if (!tabInit) {
                    tabY.set(ty);
                    tabInit = true;
                }
                tabY.update(ty, dt, 18f);
            }
        }
        int indY = Math.round(tabY.get());
        d.roundRect(px + 8, indY, sb - 16, TAB_H, 4, Theme.withAlpha(accent, 0x30));
        d.roundRect(px + 8, indY + 5, 2, TAB_H - 10, 1, accent);

        for (int i = 0; i < cats.length; i++) {
            Category c = cats[i];
            int ty = tabsTop + i * (TAB_H + 2);
            boolean hover = mx >= px + 8 && mx < px + sb - 8 && my >= ty && my < ty + TAB_H && !closing;
            float hv = a(c, "hov", hover ? 1f : 0f, 16f, dt);
            float sel = a(c, "sel", c == tab ? 1f : 0f, 16f, dt);
            if (c != tab && hv > 0.01f) {
                d.roundRect(px + 8, ty, sb - 16, TAB_H, 4, Theme.mulAlpha(0x22FFFFFF, hv));
            }
            int col = Theme.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(sel, hv * 0.6f));
            d.text(c.label, px + 20, ty + (TAB_H - d.fontHeight()) / 2 + 1, col, false);
            if (c != Category.INTERFACE) {
                String cnt = enabledCount(c);
                d.textRight(cnt, px + sb - 14, ty + (TAB_H - d.fontHeight()) / 2 + 1, Theme.TEXT_DIM, false);
            }
            hits.add(new Hit(Type.TAB, px + 8, ty, sb - 16, TAB_H, c, false));
        }

        // footer: key hint
        String key = openKeyName.get();
        int chipW = d.textWidth(key) + 10;
        int fy = py + ph - 24;
        d.card(px + 12, fy, chipW, 14, 4, Theme.CARD, Theme.BORDER);
        d.text(key, px + 17, fy + 3, Theme.TEXT_SOFT, false);
        d.text("toggles menu", px + 12, fy - 11, Theme.TEXT_DIM, false);
    }

    private String enabledCount(Category c) {
        int on = 0, total = 0;
        for (Feature f : Features.ALL) {
            if (f.category != c || f.alwaysOn) continue;
            total++;
            if (f.enabled) on++;
        }
        return on + "/" + total;
    }

    private void drawContent(Draw d, int px, int py, int pw, int ph, int mx, int my, float dt, int accent) {
        int cx = px + SIDEBAR_W + 11;
        int cw = pw - SIDEBAR_W - 22;
        listTop = py + 38;
        listBottom = py + ph - 10;

        List<Feature> list = visible();
        boolean searching = !search.trim().isEmpty();

        // title
        d.text(searching ? "Search" : tab.label, cx, py + 10, Theme.TEXT, true);
        d.text(list.size() + (list.size() == 1 ? " feature" : " features"), cx, py + 22, Theme.TEXT_DIM, false);

        // search box
        int boxW = Math.min(130, cw - 90);
        int boxX = cx + cw - boxW, boxY = py + 9;
        float focus = a(this, "searchFocus", searchFocus ? 1f : 0f, 18f, dt);
        d.card(boxX, boxY, boxW, 17, 4, Theme.FIELD, Theme.lerp(Theme.BORDER, accent, focus));
        int textY = boxY + (17 - d.fontHeight()) / 2 + 1;
        if (search.isEmpty() && !searchFocus) {
            d.text("Search...", boxX + 7, textY, Theme.TEXT_DIM, false);
        } else {
            String shown = tail(d, search, boxW - 18);
            d.text(shown, boxX + 7, textY, Theme.TEXT, false);
            if (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0) {
                int cxs = boxX + 7 + d.textWidth(shown);
                d.fill(cxs + 1, boxY + 4, cxs + 2, boxY + 13, accent);
            }
        }
        hits.add(new Hit(Type.SEARCH, boxX, boxY, boxW, 17, null, false));

        // scrollable list
        d.pushClip(cx - 4, listTop, cx + cw + 4, listBottom);
        int y = listTop - Math.round(scroll.get());
        int total = 0;
        for (Feature f : list) {
            int settingsH = settingsHeight(f);
            float ex = a(f, "exp", expanded.contains(f) ? 1f : 0f, 16f, dt);
            int h = HEADER_H + Math.round(settingsH * ex);
            if (y + h > listTop && y < listBottom) {
                drawCard(d, f, cx, y, cw, h, ex, mx, my, dt, accent);
            }
            y += h + CARD_GAP;
            total += h + CARD_GAP;
        }
        if (list.isEmpty()) {
            d.textCentered("Nothing matches your search", cx + cw / 2, listTop + 30, Theme.TEXT_DIM, false);
        }
        d.popClip();

        contentH = Math.max(0, total - CARD_GAP);
        int view = listBottom - listTop;
        float max = Math.max(0, contentH - view);
        scrollTarget = Math.max(0, Math.min(max, scrollTarget));
        scroll.update(scrollTarget, dt, 18f);

        // scrollbar
        if (max > 0) {
            int thumbH = Math.max(16, (int) (view * (view / (float) contentH)));
            int thumbY = listTop + Math.round((view - thumbH) * (scroll.get() / max));
            d.roundRect(px + pw - 6, thumbY, 2, thumbH, 1, Theme.withAlpha(accent, 0x90));
        }
    }

    private void drawCard(Draw d, Feature f, int x, int y, int w, int h, float ex,
                          int mx, int my, float dt, int accent) {
        boolean inList = my >= listTop && my < listBottom;
        boolean hoverHeader = inList && !closing && mx >= x && mx < x + w && my >= y && my < y + HEADER_H;
        float hov = a(f, "hov", hoverHeader ? 1f : 0f, 16f, dt);
        float en = a(f, "en", f.isOn() ? 1f : 0f, 16f, dt);

        int border = Theme.lerp(Theme.BORDER, accent, en * 0.55f);
        d.card(x, y, w, h, 5, Theme.lerp(Theme.CARD, Theme.CARD_HOVER, hov), border);

        // header text
        int textMax = w - 20 - (f.alwaysOn ? 24 : 64);
        d.text(fit(d, f.name, textMax), x + 10, y + 6, Theme.lerp(Theme.TEXT_SOFT, Theme.TEXT, en), false);
        d.text(fit(d, f.description, textMax), x + 10, y + 18, Theme.TEXT_DIM, false);

        hits.add(new Hit(Type.HEADER, x, y, w, HEADER_H, f, true));

        int rightEdge = x + w - 10;
        if (!f.alwaysOn) {
            toggle(d, rightEdge - 24, y + 10, 24, 12, en, accent);
            hits.add(new Hit(Type.SWITCH, rightEdge - 28, y + 4, 32, 24, f, true));
            rightEdge -= 34;
        }
        if (!f.settings.isEmpty()) chevron(d, rightEdge - 8, y + 14, ex, Theme.lerp(Theme.TEXT_DIM, Theme.TEXT, hov));

        // settings
        if (ex > 0.01f) {
            d.pushClip(x + 1, y + HEADER_H, x + w - 1, y + h - 1);
            d.fill(x + 8, y + HEADER_H, x + w - 8, y + HEADER_H + 1, Theme.BORDER);
            boolean interactive = ex > 0.9f;
            int ry = y + HEADER_H + 4;
            for (Setting s : f.settings) {
                int rh = rowHeight(s);
                if (s instanceof Setting.Bool b) boolRow(d, b, x, ry, w, interactive, dt, accent);
                else if (s instanceof Setting.Num n) numRow(d, n, x, ry, w, interactive, dt, accent);
                else if (s instanceof Setting.Mode m) modeRow(d, m, x, ry, w, interactive, dt, accent);
                ry += rh;
            }
            d.popClip();
        }
    }

    private void boolRow(Draw d, Setting.Bool b, int x, int y, int w, boolean live, float dt, int accent) {
        float v = a(b, "v", b.value ? 1f : 0f, 18f, dt);
        d.text(b.name, x + 10, y + 6, Theme.TEXT_SOFT, false);
        toggle(d, x + w - 10 - 20, y + 5, 20, 10, v, accent);
        if (live) hits.add(new Hit(Type.BOOL, x, y, w, 20, b, true));
    }

    private void modeRow(Draw d, Setting.Mode m, int x, int y, int w, boolean live, float dt, int accent) {
        d.text(m.name, x + 10, y + 6, Theme.TEXT_SOFT, false);
        String val = m.get();
        int cw = d.textWidth(val) + 16;
        int cxp = x + w - 10 - cw;
        d.card(cxp, y + 3, cw, 14, 4, Theme.FIELD, Theme.BORDER);
        int col = Features.ACCENT == m ? Theme.ACCENTS[m.index] : accent;
        d.textCentered(val, cxp + cw / 2, y + 3 + (14 - d.fontHeight()) / 2 + 1, col, false);
        if (live) hits.add(new Hit(Type.MODE, cxp, y + 3, cw, 14, m, true));
    }

    private void numRow(Draw d, Setting.Num n, int x, int y, int w, boolean live, float dt, int accent) {
        float frac = a(n, "v", (float) n.fraction(), 24f, dt);
        d.text(n.name, x + 10, y + 3, Theme.TEXT_SOFT, false);
        d.textRight(n.display(), x + w - 10, y + 3, accent, false);
        int tx = x + 10, tw = w - 20, ty = y + 17;
        d.roundRect(tx, ty, tw, 4, 2, Theme.TRACK);
        int fw = Math.max(4, Math.round(tw * frac));
        d.roundRect(tx, ty, fw, 4, 2, accent);
        int kx = Math.max(tx, Math.min(tx + tw - 8, tx + Math.round(tw * frac) - 4));
        d.roundRect(kx, ty - 2, 8, 8, 4, 0xFFFFFFFF);
        if (live) hits.add(new Hit(Type.SLIDER, tx, y + 11, tw, 14, n, true));
    }

    private void toggle(Draw d, int x, int y, int w, int h, float t, int accent) {
        d.roundRect(x, y, w, h, h / 2, Theme.lerp(0xFF2E2E3A, accent, t));
        int k = h - 4;
        int kx = x + 2 + Math.round((w - k - 4) * t);
        d.roundRect(kx, y + 2, k, k, k / 2, 0xFFFFFFFF);
    }

    /** Small chevron: points right when closed, down when open. Drawn with 1px steps. */
    private void chevron(Draw d, int cx, int cy, float open, int col) {
        if (open < 0.5f) {
            for (int i = 0; i < 4; i++) {
                d.fill(cx + i, cy - 3 + i, cx + i + 1, cy - 2 + i, col);
                d.fill(cx + i, cy + 3 - i, cx + i + 1, cy + 4 - i, col);
            }
        } else {
            for (int i = 0; i < 4; i++) {
                d.fill(cx - 3 + i, cy - 1 + i, cx - 2 + i, cy + i, col);
                d.fill(cx + 3 - i, cy - 1 + i, cx + 4 - i, cy + i, col);
            }
        }
    }

    // =====================================================================================
    // Layout helpers
    // =====================================================================================

    private List<Feature> visible() {
        String q = search.trim().toLowerCase(Locale.ROOT);
        List<Feature> out = new ArrayList<>();
        for (Feature f : Features.ALL) {
            boolean match = q.isEmpty()
                    ? f.category == tab
                    : f.name.toLowerCase(Locale.ROOT).contains(q)
                    || f.description.toLowerCase(Locale.ROOT).contains(q)
                    || f.category.label.toLowerCase(Locale.ROOT).contains(q);
            if (match) out.add(f);
        }
        return out;
    }

    private static int rowHeight(Setting s) { return s instanceof Setting.Num ? 28 : 20; }

    private static int settingsHeight(Feature f) {
        if (f.settings.isEmpty()) return 0;
        int h = 8;
        for (Setting s : f.settings) h += rowHeight(s);
        return h;
    }

    private float a(Object owner, String key, float target, float speed, float dt) {
        Smooth s = anims.computeIfAbsent(new K(owner, key), k -> new Smooth(target));
        return s.update(target, dt, speed);
    }

    private static String fit(Draw d, String s, int maxW) {
        if (d.textWidth(s) <= maxW) return s;
        while (s.length() > 1 && d.textWidth(s + "...") > maxW) s = s.substring(0, s.length() - 1);
        return s + "...";
    }

    private static String tail(Draw d, String s, int maxW) {
        while (s.length() > 1 && d.textWidth(s) > maxW) s = s.substring(1);
        return s;
    }

    // =====================================================================================
    // Input
    // =====================================================================================

    public void requestClose() {
        if (closing) return;
        closing = true;
        dragging = null;
        Config.save();
    }

    public boolean mouseClicked(double mxD, double myD, int button) {
        if (closing) return true;
        int mx = (int) mxD, my = (int) myD;

        if (mx < panelX || mx >= panelX + panelW || my < panelY || my >= panelY + panelH) {
            requestClose(); // click on the dimmed area
            return true;
        }
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.contains(mx, my)) continue;
            if (h.content() && (my < listTop || my >= listBottom)) continue;

            searchFocus = h.type() == Type.SEARCH;
            boolean secondary = button != 0;
            if (secondary && h.type() != Type.MODE && h.type() != Type.HEADER) return true;

            switch (h.type()) {
                case TAB -> {
                    tab = (Category) h.ref();
                    search = "";
                    scrollTarget = 0;
                    scroll.set(0);
                }
                case SEARCH -> { }
                case HEADER -> {
                    Feature f = (Feature) h.ref();
                    if (secondary && !f.alwaysOn) {
                        f.enabled = !f.enabled;
                        Config.save();
                    } else if (!f.settings.isEmpty()) {
                        if (!expanded.remove(f)) expanded.add(f);
                    } else if (!f.alwaysOn) {
                        f.enabled = !f.enabled;
                        Config.save();
                    }
                }
                case SWITCH -> {
                    Feature f = (Feature) h.ref();
                    f.enabled = !f.enabled;
                    Config.save();
                }
                case BOOL -> {
                    Setting.Bool b = (Setting.Bool) h.ref();
                    b.value = !b.value;
                    Config.save();
                }
                case MODE -> {
                    Setting.Mode m = (Setting.Mode) h.ref();
                    if (secondary) m.prev(); else m.next();
                    Config.save();
                }
                case SLIDER -> {
                    dragging = h;
                    applySlider(h, mx);
                }
            }
            return true;
        }
        searchFocus = false;
        return true;
    }

    public boolean mouseDragged(double mx, double my) {
        if (dragging == null) return false;
        applySlider(dragging, (int) mx);
        return true;
    }

    public boolean mouseReleased() {
        if (dragging != null) {
            dragging = null;
            Config.save();
        }
        return true;
    }

    public boolean mouseScrolled(double amount) {
        scrollTarget -= (float) (amount * 28.0);
        return true;
    }

    /** @return true if the key was consumed. */
    public boolean keyPressed(int key, boolean isOpenKey) {
        if (closing) return true;
        if (key == 256) { // ESC
            if (searchFocus) {
                searchFocus = false;
                search = "";
                scrollTarget = 0;
            } else {
                requestClose();
            }
            return true;
        }
        if (searchFocus) {
            if (key == 259 && !search.isEmpty()) { // backspace
                search = search.substring(0, search.length() - 1);
                scrollTarget = 0;
                return true;
            }
            if (isOpenKey && !(key >= 32 && key <= 96)) {
                requestClose();
                return true;
            }
            return true; // swallow everything else while typing
        }
        if (isOpenKey) {
            requestClose();
            return true;
        }
        return false;
    }

    public boolean charTyped(int codePoint) {
        if (closing || codePoint < 32 || codePoint == 127) return false;
        if (!searchFocus) searchFocus = true; // start typing anywhere to search
        if (search.length() < 24) {
            search += new String(Character.toChars(codePoint));
            scrollTarget = 0;
        }
        return true;
    }

    private void applySlider(Hit h, int mx) {
        Setting.Num n = (Setting.Num) h.ref();
        double t = Math.max(0.0, Math.min(1.0, (mx - h.x()) / (double) h.w()));
        n.set(n.min + t * (n.max - n.min));
    }
}
