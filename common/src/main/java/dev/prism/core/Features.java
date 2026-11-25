package dev.prism.core;

import dev.prism.core.Feature.Category;

import java.util.ArrayList;
import java.util.List;

/** All features. Add a new one by registering it here (and rendering it in HudRenderer if it is a HUD widget). */
public final class Features {
    private Features() {}

    public static final List<Feature> ALL = new ArrayList<>();

    private static final String[] CORNERS = {"Top Left", "Top Right", "Bottom Left", "Bottom Right"};

    private static Feature add(Feature f) {
        ALL.add(f);
        return f;
    }

    // ---- HUD ----
    public static final Feature FPS = add(new Feature("fps", "FPS Counter",
            "Shows your current frames per second.", Category.HUD, true, false));
    public static final Setting.Mode FPS_POS = FPS.mode("Position", 0, CORNERS);
    public static final Setting.Bool FPS_BG = FPS.bool("Background", true);

    public static final Feature COORDS = add(new Feature("coords", "Coordinates",
            "Shows your XYZ position and facing direction.", Category.HUD, true, false));
    public static final Setting.Mode COORDS_POS = COORDS.mode("Position", 0, CORNERS);
    public static final Setting.Bool COORDS_BG = COORDS.bool("Background", true);
    public static final Setting.Num COORDS_DEC = COORDS.num("Decimals", 0, 2, 1, 1, "");
    public static final Setting.Bool COORDS_FACING = COORDS.bool("Show direction", true);

    public static final Feature CPS = add(new Feature("cps", "CPS Counter",
            "Clicks per second for left and right mouse buttons.", Category.HUD, false, false));
    public static final Setting.Mode CPS_POS = CPS.mode("Position", 0, CORNERS);
    public static final Setting.Bool CPS_BG = CPS.bool("Background", true);
    public static final Setting.Bool CPS_RIGHT = CPS.bool("Show right click", true);

    public static final Feature KEYS = add(new Feature("keys", "Keystrokes",
            "Live display of movement keys and mouse buttons.", Category.HUD, false, false));
    public static final Setting.Mode KEYS_POS = KEYS.mode("Position", 2, CORNERS);
    public static final Setting.Bool KEYS_MOUSE = KEYS.bool("Show mouse", true);
    public static final Setting.Bool KEYS_SPACE = KEYS.bool("Show space", true);

    public static final Feature CLOCK = add(new Feature("clock", "Clock",
            "Shows your computer's local time.", Category.HUD, false, false));
    public static final Setting.Mode CLOCK_POS = CLOCK.mode("Position", 1, CORNERS);
    public static final Setting.Bool CLOCK_BG = CLOCK.bool("Background", true);
    public static final Setting.Bool CLOCK_24H = CLOCK.bool("24 hour", true);

    // ---- Visual ----
    public static final Feature ZOOM = add(new Feature("zoom", "Zoom",
            "Hold the Zoom key (default C) to zoom in. Rebind in Controls.", Category.VISUAL, true, false));
    public static final Setting.Num ZOOM_FOV = ZOOM.num("Zoom FOV", 30, 70, 5, 30, "");

    public static final Feature SWING = add(new Feature("swing", "Swing Speed",
            "Changes how fast your arm swing animation plays. Visual only.", Category.VISUAL, false, false));
    public static final Setting.Num SWING_SPEED = SWING.num("Speed", 0.5, 3.0, 0.1, 1.0, "x");

    public static final Feature HANDGLOW = add(new Feature("handglow", "Hand Glow (Experimental)",
            "Glowing outline around the item in your hand (first person). Visual only.", Category.VISUAL, false, false));
    public static final Setting.Num HANDGLOW_WIDTH = HANDGLOW.num("Width", 1, 10, 1, 4, "");
    public static final Setting.Mode HANDGLOW_COLOR = HANDGLOW.mode("Color", 0, "White", "Red");
    public static final Setting.Bool HANDGLOW_PULSE = HANDGLOW.bool("Pulse", true);

    // ---- Interface ----
    public static final Feature UI = add(new Feature("ui", "Menu Style",
            "Customize how this menu looks.", Category.INTERFACE, true, true));
    public static final Setting.Mode ACCENT = UI.mode("Accent color", 0, Theme.ACCENT_NAMES);
    public static final Setting.Num DIM = UI.num("Background dim", 0, 80, 5, 45, "%");
    public static final Setting.Bool ANIMATIONS = UI.bool("Animations", true);
}
