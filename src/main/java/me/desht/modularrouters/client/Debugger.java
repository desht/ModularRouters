package me.desht.modularrouters.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
public class Debugger {
    /**
     * This can be used as an IntelliJ break condition to ensure mouse pointer is
     * released when client breakpoints are hit (for Linux in particular).
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean ungrabMouse() {
        Minecraft.getInstance().mouseHandler.releaseMouse();
        return true;
    }
}
