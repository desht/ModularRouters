package me.desht.modularrouters.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

public class DebugHelper {
    /**
     * This can be used as an IntelliJ break condition to ensure mouse pointer is
     * released when client breakpoints are hit (for Linux in particular).
     */
    @SideOnly(Side.CLIENT)
    public static boolean ungrabMouse() {
        Mouse.setGrabbed(false);
        return true;
    }
}
