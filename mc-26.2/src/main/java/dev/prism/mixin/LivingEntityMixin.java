package dev.prism.mixin;

import dev.prism.core.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales the length of the swing animation for the local player only.
 * require = 0: if a Minecraft update renames the target method, this is skipped instead of crashing.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true, require = 0)
    private void prism$swingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!Features.SWING.enabled) return;
        if ((Object) this != Minecraft.getInstance().player) return;
        double speed = Features.SWING_SPEED.value;
        cir.setReturnValue(Math.max(1, (int) Math.round(cir.getReturnValue() / speed)));
    }
}
