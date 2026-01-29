package me.desht.modularrouters.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyMapping.Category CATEGORY = new KeyMapping.Category(ModularRouters.id("key"));

    public static KeyMapping keybindConfigure;
    public static KeyMapping keybindModuleInfo;

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        KeyBindings.keybindConfigure = new KeyMapping("key.modularrouters.configure", KeyConflictContext.GUI,
                InputConstants.getKey(new KeyEvent(GLFW.GLFW_KEY_C, -1, 0)), CATEGORY);
        KeyBindings.keybindModuleInfo = new KeyMapping("key.modularrouters.moduleInfo", KeyConflictContext.GUI,
                InputConstants.getKey(new KeyEvent(GLFW.GLFW_KEY_I, -1, 0)), CATEGORY);

        event.register(KeyBindings.keybindConfigure);
        event.register(KeyBindings.keybindModuleInfo);
    }
}
