package dev.prism.core;

/** Zoom by temporarily lowering the FOV option while the zoom key is held. No mixins needed. */
public final class ZoomController {
    public interface Fov {
        int get();

        void set(int value);
    }

    private int saved = -1;

    public void tick(boolean keyHeld, Fov fov) {
        boolean want = Features.ZOOM.enabled && keyHeld;
        if (want) {
            if (saved < 0) saved = fov.get();
            int target = (int) Features.ZOOM_FOV.value;
            if (fov.get() != target) fov.set(target);
        } else if (saved >= 0) {
            fov.set(saved);
            saved = -1;
        }
    }

    /** Call on shutdown so the zoomed FOV never gets saved into options.txt. */
    public void reset(Fov fov) {
        if (saved >= 0) {
            fov.set(saved);
            saved = -1;
        }
    }
}
