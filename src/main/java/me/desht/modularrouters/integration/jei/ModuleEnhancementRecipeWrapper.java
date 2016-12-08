package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.ModuleEnhancementRecipe;
import me.desht.modularrouters.util.MiscUtil;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ModuleEnhancementRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper, ITooltipCallback<ItemStack> {
    private final ModuleEnhancementRecipe recipe;
    private final String name;
    private final String[] description;

    public ModuleEnhancementRecipeWrapper(ModuleEnhancementRecipe recipe) {
        this.recipe = recipe;
        this.name = I18n.format("jei.enhancement." + recipe.getRecipeId() + ".name");
        this.description = MiscUtil.splitLong("jei.enhancement." + recipe.getRecipeId() + ".description", 30);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        IStackHelper stackHelper = JEIModularRoutersPlugin.registry.getJeiHelpers().getStackHelper();
        ItemStack recipeOutput = recipe.getRecipeOutput();

        try {
            List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(Arrays.asList(recipe.getInput()));
            ingredients.setInputLists(ItemStack.class, inputs);
            if (recipeOutput != null) {
                ingredients.setOutput(ItemStack.class, recipeOutput);
            }
        } catch (RuntimeException e) {
            ModularRouters.logger.warn("broken recipe?");
        }
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int x = (ModuleEnhancementRecipeCategory.GUI_WIDTH - minecraft.fontRendererObj.getStringWidth(name)) / 2;
        minecraft.fontRendererObj.drawString(name, x, -4, Color.darkGray.getRGB());
        for (int i = 0; i < description.length; i++) {
            minecraft.fontRendererObj.drawString(description[i], 11, 62 + (i * 8), Color.black.getRGB());
        }
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (slotIndex == 0 && !input) {
            tooltip.add(TextFormatting.GREEN + TextFormatting.BOLD.toString() + name);
        }
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }
}
