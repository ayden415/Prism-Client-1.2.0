package dev.prism.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/** Saves feature on/off state and settings to a plain .properties file. */
public final class Config {
    private Config() {}

    private static Path path;

    public static void init(Path file) {
        path = file;
        load();
    }

    private static String key(Feature f, Setting s) {
        return f.id + "." + s.name.toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static void load() {
        if (path == null || !Files.exists(path)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Feature f : Features.ALL) {
            String en = p.getProperty(f.id + ".enabled");
            if (en != null && !f.alwaysOn) f.enabled = Boolean.parseBoolean(en);
            for (Setting s : f.settings) {
                String v = p.getProperty(key(f, s));
                if (v != null) s.deserialize(v);
            }
        }
    }

    public static void save() {
        if (path == null) return;
        Properties p = new Properties();
        for (Feature f : Features.ALL) {
            p.setProperty(f.id + ".enabled", Boolean.toString(f.enabled));
            for (Setting s : f.settings) p.setProperty(key(f, s), s.serialize());
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            p.store(out, "Prism Client");
        } catch (IOException ignored) {
        }
    }
}
