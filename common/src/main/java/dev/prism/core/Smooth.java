package dev.prism.core;

/** A value that eases toward a target (frame-rate independent exponential smoothing). */
public final class Smooth {
    private float value;

    public Smooth(float start) { this.value = start; }

    public float get() { return value; }

    public void set(float v) { value = v; }

    public float update(float target, float dt, float speed) {
        if (!Features.ANIMATIONS.value) {
            value = target;
            return value;
        }
        value += (target - value) * (1f - (float) Math.exp(-speed * dt));
        if (Math.abs(target - value) < 0.002f) value = target;
        return value;
    }
}
