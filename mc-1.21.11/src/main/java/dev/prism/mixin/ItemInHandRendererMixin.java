package dev.prism.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.prism.client.HandGlow;
import dev.prism.core.Features;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Wraps the single call that draws the held item in first person, so Hand Glow can draw its
 * outline copies just before it. require = 0: if the call is not found, this does nothing.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Redirect(
            method = "renderItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"),
            require = 0)
    private void prism$handGlow(ItemStackRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                int light, int overlay, int outline) {
        if (Features.HANDGLOW.enabled) {
            try {
                HandGlow.draw(state, poseStack, collector);
            } catch (RuntimeException ignored) {
                // never let an experimental effect crash the game
            }
        }
        state.submit(poseStack, collector, light, overlay, outline);
    }
}
