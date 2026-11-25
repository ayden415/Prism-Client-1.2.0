package dev.prism.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Every call that has moved around between 26.x releases lives here, so a compile error
 * after a Minecraft update is a one-line fix in this file.
 */
final class Compat {
    private Compat() {}

    static boolean noScreenOpen(Minecraft mc) { return mc.gui.screen() == null; }

    static void show(Minecraft mc, Screen screen) {
        // 26.2 renamed Minecraft#setScreen to setScreenAndShow.
        // If your mappings say otherwise, try: mc.gui.setScreen(screen);
        mc.setScreenAndShow(screen);
    }
}
