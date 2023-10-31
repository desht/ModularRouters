package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModularRouters.MODID);

    public static final RegistryObject<SoundEvent> ERROR = register("error");
    public static final RegistryObject<SoundEvent> SUCCESS = register("success");
    public static final RegistryObject<SoundEvent> THUD = register("thud");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RL(name)));
    }
}
