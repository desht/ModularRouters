package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ModularRouters.MODID);

    public static final Supplier<SoundEvent> ERROR = register("error");
    public static final Supplier<SoundEvent> SUCCESS = register("success");
    public static final Supplier<SoundEvent> THUD = register("thud");

    private static Supplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RL(name)));
    }
}
