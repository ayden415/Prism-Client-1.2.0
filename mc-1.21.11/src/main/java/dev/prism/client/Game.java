package dev.prism.client;

import dev.prism.core.GameInfo;
import net.minecraft.client.Minecraft;

/** Reads game state for the HUD (1.21.11, Mojang mappings). */
final class Game implements GameInfo {
    private final Minecraft mc = Minecraft.getInstance();

    @Override public boolean inWorld() { return mc.player != null && mc.level != null; }

    @Override public int fps() { return mc.getFps(); }

    @Override public double x() { return mc.player.getX(); }

    @Override public double y() { return mc.player.getY(); }

    @Override public double z() { return mc.player.getZ(); }

    @Override public float yaw() { return mc.player.getYRot(); }

    @Override public boolean forward() { return mc.options.keyUp.isDown(); }

    @Override public boolean back() { return mc.options.keyDown.isDown(); }

    @Override public boolean left() { return mc.options.keyLeft.isDown(); }

    @Override public boolean right() { return mc.options.keyRight.isDown(); }

    @Override public boolean jump() { return mc.options.keyJump.isDown(); }

    @Override public boolean attack() { return mc.options.keyAttack.isDown(); }

    @Override public boolean use() { return mc.options.keyUse.isDown(); }
}
