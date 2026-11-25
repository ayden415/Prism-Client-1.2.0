package dev.prism.core;

/** Wraps a Draw and multiplies every alpha by a factor (used for the menu fade-in/out). */
final class Fade implements Draw {
    private Draw d;
    private float a = 1f;

    Fade with(Draw d, float a) {
        this.d = d;
        this.a = a;
        return this;
    }

    @Override public void fill(int x1, int y1, int x2, int y2, int argb) {
        int c = Theme.mulAlpha(argb, a);
        if ((c >>> 24) == 0) return;
        d.fill(x1, y1, x2, y2, c);
    }

    @Override public void text(String s, int x, int y, int argb, boolean shadow) {
        int c = Theme.mulAlpha(argb, a);
        // Minecraft treats very low alpha text as opaque, so skip it instead.
        if ((c >>> 24) < 8) return;
        d.text(s, x, y, c, shadow);
    }

    @Override public int textWidth(String s) { return d.textWidth(s); }

    @Override public int fontHeight() { return d.fontHeight(); }

    @Override public void pushClip(int x1, int y1, int x2, int y2) { d.pushClip(x1, y1, x2, y2); }

    @Override public void popClip() { d.popClip(); }
}
