package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.modularrouters.util.MiscUtil.RL;

@ObjectHolder(ModularRouters.MODID)
@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSounds {
    public static final SoundEvent ERROR = null;
    public static final SoundEvent SUCCESS = null;
    public static final SoundEvent THUD = null;

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        register(event.getRegistry(), RL("error"));
        register(event.getRegistry(), RL("success"));
        register(event.getRegistry(), RL("thud"));
    }

    private static void register(IForgeRegistry<SoundEvent> registry, ResourceLocation name) {
        registry.register(new SoundEvent(name).setRegistryName(name));
    }
}
