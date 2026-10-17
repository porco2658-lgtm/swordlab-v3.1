package com.swordlab.module;

/** Rotation that gets written into outgoing movement packets only (camera is untouched). */
public final class AimState {
    public static boolean active;
    public static float yaw, pitch;

    private AimState() {}
}
