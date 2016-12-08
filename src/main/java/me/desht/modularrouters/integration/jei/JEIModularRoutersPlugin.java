package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.ModularRouters;
import mezz.jei.api.*;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JEIModularRoutersPlugin extends BlankModPlugin {
    public static IModRegistry registry;

    @Override
    public void register(IModRegistry registry) {
        JEIModularRoutersPlugin.registry = registry;

        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registry.addRecipeCategories(
                new ModuleEnhancementRecipeCategory(guiHelper)
        );

        registry.addRecipeHandlers(
                new RedstoneEnhancementRecipeHandler(),
                new RegulatorEnhancementRecipeHandler()
        );

        registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.CRAFTING_TABLE), ModularRouters.modId + ".moduleEnhancement");
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerWorkbench.class, ModularRouters.modId + ".moduleEnhancement", 1, 9, 10, 36);
    }
}
