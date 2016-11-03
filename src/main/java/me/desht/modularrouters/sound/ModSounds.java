package me.desht.modularrouters.sound;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModSounds {
    private ModSounds() {}

    public static void init() {
        String[] sounds = {
                "error",
                "success",
                "thud"
        };

        for (String s : sounds) {
            ResourceLocation loc = new ResourceLocation(ModularRouters.modId + ":" + s);
            GameRegistry.register(new SoundEvent(loc), loc);
        }
    }
}
