package dev.prism.core;

import java.util.ArrayList;
import java.util.List;

/** One toggleable feature (HUD widget, visual tweak, ...) shown as a card in the menu. */
public final class Feature {
    public enum Category {
        HUD("HUD"), VISUAL("Visual"), INTERFACE("Interface");

        public final String label;

        Category(String label) { this.label = label; }
    }

    public final String id, name, description;
    public final Category category;
    /** Always-on features (like the Interface settings) have no on/off switch. */
    public final boolean alwaysOn;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    public Feature(String id, String name, String description, Category category, boolean enabled, boolean alwaysOn) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
        this.alwaysOn = alwaysOn;
    }

    public boolean isOn() { return alwaysOn || enabled; }

    public Setting.Bool bool(String n, boolean def) {
        Setting.Bool s = new Setting.Bool(n, def);
        settings.add(s);
        return s;
    }

    public Setting.Num num(String n, double min, double max, double step, double def, String suffix) {
        Setting.Num s = new Setting.Num(n, min, max, step, def, suffix);
        settings.add(s);
        return s;
    }

    public Setting.Mode mode(String n, int def, String... options) {
        Setting.Mode s = new Setting.Mode(n, def, options);
        settings.add(s);
        return s;
    }
}
