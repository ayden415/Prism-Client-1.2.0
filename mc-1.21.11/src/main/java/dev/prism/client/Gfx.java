package dev.prism.client;

import dev.prism.core.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Adapts Minecraft 1.21.11's GuiGraphics to the version-independent Draw interface. */
final class Gfx implements Draw {
    private GuiGraphics g;
    private final Minecraft mc = Minecraft.getInstance();

    Gfx begin(GuiGraphics g) {
        this.g = g;
        return this;
    }

    @Override public void fill(int x1, int y1, int x2, int y2, int argb) { g.fill(x1, y1, x2, y2, argb); }

    @Override public void text(String s, int x, int y, int argb, boolean shadow) {
        g.drawString(mc.font, s, x, y, argb, shadow);
    }

    @Override public int textWidth(String s) { return mc.font.width(s); }

    @Override public int fontHeight() { return mc.font.lineHeight; }

    @Override public void pushClip(int x1, int y1, int x2, int y2) { g.enableScissor(x1, y1, x2, y2); }

    @Override public void popClip() { g.disableScissor(); }
}
