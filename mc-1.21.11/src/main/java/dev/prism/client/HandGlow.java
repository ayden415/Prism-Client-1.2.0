package dev.prism.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.prism.core.Features;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Experimental first-person item outline.
 * Draws the held item several extra times in a ring around the real one, pushed slightly away from
 * the camera, fully lit and washed with the overlay color. The real item is drawn on top afterwards,
 * so only the ring around its silhouette stays visible.
 */
public final class HandGlow {
    private static final int RING = 12;
    private static final int FULL_BRIGHT = 0xF000F0;

    private HandGlow() {}

    public static void draw(ItemStackRenderState state, PoseStack ps, SubmitNodeCollector collector) {
        double width = 0.003 * Features.HANDGLOW_WIDTH.value;
        if (Features.HANDGLOW_PULSE.value) {
            width *= 0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 180.0);
        }
        // Overlay texture: row 10, column 15 = 75% white; row 3 = red hurt tint. (pack = u | v << 16)
        int overlay = Features.HANDGLOW_COLOR.index == 1 ? (3 << 16) : ((10 << 16) | 15);

        // Work in eye space so the ring is always a ring on screen, then convert to the local pose.
        Matrix4f inverse = new Matrix4f(ps.last().pose()).invert();
        Vector3f off = new Vector3f();
        for (int i = 0; i < RING; i++) {
            double a = i * (Math.PI * 2.0 / RING);
            off.set((float) (Math.cos(a) * width), (float) (Math.sin(a) * width), -0.004f);
            inverse.transformDirection(off);
            ps.pushPose();
            ps.translate(off.x, off.y, off.z);
            state.submit(ps, collector, FULL_BRIGHT, overlay, 0);
            ps.popPose();
        }
    }
}
