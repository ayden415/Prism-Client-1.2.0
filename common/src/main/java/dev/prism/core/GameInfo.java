package dev.prism.core;

/** What the HUD needs to know about the game. Implemented per Minecraft version (Game.java). */
public interface GameInfo {
    boolean inWorld();

    int fps();

    double x();

    double y();

    double z();

    /** Player yaw in degrees (Minecraft convention: 0 = south). */
    float yaw();

    boolean forward();

    boolean back();

    boolean left();

    boolean right();

    boolean jump();

    boolean attack();

    boolean use();
}
