package me.desht.modularrouters.sound;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class MRSoundEvents {
    public static final SoundEvent error = getRegisteredSoundEvent("error");
    public static final SoundEvent success = getRegisteredSoundEvent("success");
    public static final SoundEvent thud = getRegisteredSoundEvent("thud");

    private static SoundEvent getRegisteredSoundEvent(String name) {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation(ModularRouters.MODID + ":" + name));
    }
}
