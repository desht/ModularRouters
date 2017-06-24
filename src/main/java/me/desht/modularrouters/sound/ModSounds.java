package me.desht.modularrouters.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModSounds {
    private ModSounds() {}

    public static void init() {
        String[] sounds = {
                "error",
                "success",
                "thud"
        };

        for (String s : sounds) {
            ResourceLocation loc = RL(s);
            ForgeRegistries.SOUND_EVENTS.register(new SoundEvent(loc).setRegistryName(loc));
        }
    }
}
