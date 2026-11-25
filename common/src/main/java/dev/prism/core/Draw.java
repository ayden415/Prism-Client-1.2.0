package dev.prism.core;

/**
 * The only drawing API the shared UI code is allowed to use.
 * Each Minecraft version implements this in a tiny adapter (Gfx.java).
 * Colors are ARGB ints. Coordinates are GUI-scaled pixels.
 */
public interface Draw {
    void fill(int x1, int y1, int x2, int y2, int argb);

    void text(String s, int x, int y, int argb, boolean shadow);

    int textWidth(String s);

    int fontHeight();

    void pushClip(int x1, int y1, int x2, int y2);

    void popClip();

    /** Filled rectangle with rounded corners, built from non-overlapping strips (alpha safe). */
    default void roundRect(int x, int y, int w, int h, int r, int argb) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            fill(x, y, x + w, y + h, argb);
            return;
        }
        fill(x, y + r, x + w, y + h - r, argb);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            fill(x + inset, y + i, x + w - inset, y + i + 1, argb);
            fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, argb);
        }
    }

    /** Rounded rect with a 1px border. Use opaque fill colors. */
    default void card(int x, int y, int w, int h, int r, int fill, int border) {
        roundRect(x, y, w, h, r, border);
        roundRect(x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    /** Soft drop shadow made of stacked translucent rounded rects. */
    default void shadow(int x, int y, int w, int h, int r, int layers, int alpha) {
        for (int i = layers; i >= 1; i--) {
            roundRect(x - i, y - i, w + 2 * i, h + 2 * i, r + i, alpha << 24);
        }
    }

    default void textRight(String s, int right, int y, int argb, boolean shadow) {
        text(s, right - textWidth(s), y, argb, shadow);
    }

    default void textCentered(String s, int cx, int y, int argb, boolean shadow) {
        text(s, cx - textWidth(s) / 2, y, argb, shadow);
    }
}
