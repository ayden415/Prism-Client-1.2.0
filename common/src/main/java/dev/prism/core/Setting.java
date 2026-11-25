package dev.prism.core;

import java.util.Locale;

public abstract class Setting {
    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public abstract String serialize();

    public abstract void deserialize(String s);

    public static final class Bool extends Setting {
        public boolean value;

        public Bool(String name, boolean def) {
            super(name);
            this.value = def;
        }

        @Override public String serialize() { return Boolean.toString(value); }

        @Override public void deserialize(String s) { value = Boolean.parseBoolean(s); }
    }

    public static final class Num extends Setting {
        public final double min, max, step;
        public final String suffix;
        public double value;

        public Num(String name, double min, double max, double step, double def, String suffix) {
            super(name);
            this.min = min;
            this.max = max;
            this.step = step;
            this.suffix = suffix;
            set(def);
        }

        public void set(double v) {
            v = Math.max(min, Math.min(max, v));
            if (step > 0) v = Math.round((v - min) / step) * step + min;
            value = Math.max(min, Math.min(max, v));
        }

        public double fraction() {
            return max == min ? 0 : (value - min) / (max - min);
        }

        public String display() {
            boolean whole = step >= 1 && step == Math.rint(step);
            String v = whole ? Long.toString(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
            return v + suffix;
        }

        @Override public String serialize() { return Double.toString(value); }

        @Override public void deserialize(String s) {
            try { set(Double.parseDouble(s)); } catch (NumberFormatException ignored) { }
        }
    }

    public static final class Mode extends Setting {
        public final String[] options;
        public int index;

        public Mode(String name, int def, String... options) {
            super(name);
            this.options = options;
            this.index = Math.max(0, Math.min(options.length - 1, def));
        }

        public String get() { return options[index]; }

        public void next() { index = (index + 1) % options.length; }

        public void prev() { index = (index + options.length - 1) % options.length; }

        @Override public String serialize() { return Integer.toString(index); }

        @Override public void deserialize(String s) {
            try { index = Math.max(0, Math.min(options.length - 1, Integer.parseInt(s))); }
            catch (NumberFormatException ignored) { }
        }
    }
}
