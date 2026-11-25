package dev.prism.core;

public final class Theme {
    private Theme() {}

    public static final String[] ACCENT_NAMES = {"Violet", "Cyan", "Rose", "Lime", "Amber", "Sky"};
    public static final int[] ACCENTS = {
            0xFF8B5CF6, 0xFF22D3EE, 0xFFFB7185, 0xFFA3E635, 0xFFFBBF24, 0xFF38BDF8
    };

    public static final int PANEL = 0xF2111116;
    public static final int SIDEBAR = 0xFF0C0C10;
    public static final int BORDER = 0xFF24242D;
    public static final int CARD = 0xFF181820;
    public static final int CARD_HOVER = 0xFF1F1F29;
    public static final int FIELD = 0xFF121218;
    public static final int TRACK = 0xFF2B2B37;

    public static final int TEXT = 0xFFF1F1F6;
    public static final int TEXT_SOFT = 0xFFB9B9C8;
    public static final int TEXT_DIM = 0xFF7C7C8E;

    public static final int HUD_BG = 0x99101016;

    public static int accent() {
        int i = Features.ACCENT.index;
        return ACCENTS[Math.max(0, Math.min(ACCENTS.length - 1, i))];
    }

    public static int withAlpha(int argb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (argb & 0xFFFFFF);
    }

    public static int mulAlpha(int argb, float m) {
        int a = (int) ((argb >>> 24) * Math.max(0f, Math.min(1f, m)));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (int) ((a >>> 24) + ((b >>> 24) - (a >>> 24)) * t);
        int ar = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int ag = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int ab = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (aa << 24) | (ar << 16) | (ag << 8) | ab;
    }
}
