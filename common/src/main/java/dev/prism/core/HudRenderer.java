package dev.prism.core;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {
    private HudRenderer() {}

    private static final int MARGIN = 6, GAP = 4;
    private static final String[] DIRS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
    private static final DateTimeFormatter T24 = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter T12 = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private static final ClickTracker CLICKS = new ClickTracker();
    private static final Smooth[] KEY = new Smooth[7];
    private static long lastNanos = System.nanoTime();

    static {
        for (int i = 0; i < KEY.length; i++) KEY[i] = new Smooth(0f);
    }

    private interface Panel {
        int w(Draw d);

        int h(Draw d);

        void draw(Draw d, int x, int y);
    }

    public static void render(Draw d, GameInfo g, int sw, int sh) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastNanos) / 1e9f);
        lastNanos = now;
        if (!g.inWorld()) return;

        CLICKS.poll(g.attack(), g.use());

        List<List<Panel>> corners = new ArrayList<>();
        for (int i = 0; i < 4; i++) corners.add(new ArrayList<>());

        if (Features.FPS.enabled) {
            corners.get(Features.FPS_POS.index)
                    .add(textPanel("FPS", Integer.toString(g.fps()), Features.FPS_BG.value));
        }
        if (Features.COORDS.enabled) {
            int dec = (int) Features.COORDS_DEC.value;
            String fmt = "%." + dec + "f";
            String v = String.format(Locale.ROOT, fmt + " / " + fmt + " / " + fmt, g.x(), g.y(), g.z());
            if (Features.COORDS_FACING.value) v += "  " + DIRS[Math.floorMod(Math.round(g.yaw() / 45f), 8)];
            corners.get(Features.COORDS_POS.index).add(textPanel("XYZ", v, Features.COORDS_BG.value));
        }
        if (Features.CPS.enabled) {
            String v = Integer.toString(CLICKS.left());
            if (Features.CPS_RIGHT.value) v += " | " + CLICKS.right();
            corners.get(Features.CPS_POS.index).add(textPanel("CPS", v, Features.CPS_BG.value));
        }
        if (Features.CLOCK.enabled) {
            String v = LocalTime.now().format(Features.CLOCK_24H.value ? T24 : T12);
            corners.get(Features.CLOCK_POS.index).add(textPanel("TIME", v, Features.CLOCK_BG.value));
        }
        if (Features.KEYS.enabled) {
            boolean[] down = {g.forward(), g.left(), g.back(), g.right(), g.attack(), g.use(), g.jump()};
            for (int i = 0; i < KEY.length; i++) KEY[i].update(down[i] ? 1f : 0f, dt, 24f);
            corners.get(Features.KEYS_POS.index).add(keystrokes());
        }

        for (int c = 0; c < 4; c++) {
            boolean right = (c & 1) == 1;
            boolean bottom = c >= 2;
            int y = bottom ? sh - MARGIN : MARGIN;
            for (Panel p : corners.get(c)) {
                int w = p.w(d), h = p.h(d);
                int x = right ? sw - MARGIN - w : MARGIN;
                if (bottom) {
                    y -= h;
                    p.draw(d, x, y);
                    y -= GAP;
                } else {
                    p.draw(d, x, y);
                    y += h + GAP;
                }
            }
        }
    }

    private static Panel textPanel(String label, String value, boolean bg) {
        return new Panel() {
            @Override public int w(Draw d) { return 8 + d.textWidth(label) + 5 + d.textWidth(value) + 7; }

            @Override public int h(Draw d) { return d.fontHeight() + 8; }

            @Override public void draw(Draw d, int x, int y) {
                int w = w(d), h = h(d);
                int accent = Theme.accent();
                if (bg) {
                    d.roundRect(x, y, w, h, 3, Theme.HUD_BG);
                    d.roundRect(x, y + 2, 2, h - 4, 1, accent);
                }
                int ty = y + (h - d.fontHeight()) / 2 + 1;
                d.text(label, x + 8, ty, accent, !bg);
                d.text(value, x + 8 + d.textWidth(label) + 5, ty, Theme.TEXT, !bg);
            }
        };
    }

    private static Panel keystrokes() {
        final boolean mouse = Features.KEYS_MOUSE.value, space = Features.KEYS_SPACE.value;
        return new Panel() {
            @Override public int w(Draw d) { return 64; }

            @Override public int h(Draw d) { return 42 + (mouse ? 22 : 0) + (space ? 12 : 0); }

            @Override public void draw(Draw d, int x, int y) {
                key(d, x + 22, y, 20, 20, "W", KEY[0].get());
                key(d, x, y + 22, 20, 20, "A", KEY[1].get());
                key(d, x + 22, y + 22, 20, 20, "S", KEY[2].get());
                key(d, x + 44, y + 22, 20, 20, "D", KEY[3].get());
                int cy = y + 44;
                if (mouse) {
                    key(d, x, cy, 31, 20, "LMB", KEY[4].get());
                    key(d, x + 33, cy, 31, 20, "RMB", KEY[5].get());
                    cy += 22;
                }
                if (space) key(d, x, cy, 64, 10, null, KEY[6].get());
            }
        };
    }

    private static void key(Draw d, int x, int y, int w, int h, String label, float t) {
        d.roundRect(x, y, w, h, 3, Theme.lerp(Theme.HUD_BG, Theme.withAlpha(Theme.accent(), 0xCC), t));
        int col = Theme.lerp(0xFFC8C8D2, 0xFFFFFFFF, t);
        if (label == null) {
            d.fill(x + w / 2 - 12, y + h / 2, x + w / 2 + 12, y + h / 2 + 1, col);
        } else {
            d.textCentered(label, x + w / 2, y + (h - d.fontHeight()) / 2 + 1, col, false);
        }
    }
}
