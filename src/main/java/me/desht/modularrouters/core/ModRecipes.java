package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.EnchantModuleRecipe;
import me.desht.modularrouters.recipe.ResetModuleRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.modularrouters.util.MiscUtil.RL;

@ObjectHolder(ModularRouters.MODID)
public class ModRecipes {
    public static final IRecipeSerializer<EnchantModuleRecipe> MODULE_ENCHANT = null;
    public static final IRecipeSerializer<EnchantModuleRecipe> MODULE_RESET = null;

    @Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(new SpecialRecipeSerializer<>(EnchantModuleRecipe::new).setRegistryName(RL("module_enchant")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(ResetModuleRecipe::new).setRegistryName(RL("module_reset")));
        }
    }
}
