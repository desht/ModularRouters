package me.desht.modularrouters.client;

import org.lwjgl.input.Mouse;

public class DebugHelper {
    public static boolean ungrabMouse() {
        Mouse.setGrabbed(false);
        return true;
    }
}
