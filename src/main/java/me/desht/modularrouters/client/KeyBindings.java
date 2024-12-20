package me.desht.modularrouters.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyMapping keybindConfigure;
    public static KeyMapping keybindModuleInfo;

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        KeyBindings.keybindConfigure = new KeyMapping("key.modularrouters.configure", KeyConflictContext.GUI,
                InputConstants.getKey(GLFW.GLFW_KEY_C, -1), "key.modularrouters.category");
        KeyBindings.keybindModuleInfo = new KeyMapping("key.modularrouters.moduleInfo", KeyConflictContext.GUI,
                InputConstants.getKey(GLFW.GLFW_KEY_I, -1), "key.modularrouters.category");

        event.register(KeyBindings.keybindConfigure);
        event.register(KeyBindings.keybindModuleInfo);
    }
}
