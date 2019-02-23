package me.desht.modularrouters.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    public static KeyBinding keybindConfigure;

    public static void registerKeyBindings() {
        keybindConfigure = new KeyBinding("key.modularrouters.configure", KeyConflictContext.GUI,
                InputMappings.getInputByCode(GLFW.GLFW_KEY_C, -1), "key.modularrouters.category");

        ClientRegistry.registerKeyBinding(keybindConfigure);
    }
}
