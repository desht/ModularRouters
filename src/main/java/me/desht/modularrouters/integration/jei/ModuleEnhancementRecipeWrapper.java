package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.recipe.enhancement.ModuleEnhancementRecipe;
import me.desht.modularrouters.util.MiscUtil;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.List;

public class ModuleEnhancementRecipeWrapper extends BlankRecipeWrapper
        implements ICustomCraftingRecipeWrapper, IShapedCraftingRecipeWrapper, ITooltipCallback<ItemStack> {
    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;

    private final IJeiHelpers helpers;
    private final ModuleEnhancementRecipe recipe;
    private final String name;
    private final String[] description;

    public ModuleEnhancementRecipeWrapper(IJeiHelpers helpers, ModuleEnhancementRecipe recipe) {
        this.helpers = helpers;
        this.recipe = recipe;
        this.name = I18n.format("jei.enhancement." + recipe.getRecipeId() + ".name");
        this.description = MiscUtil.splitLong("jei.enhancement." + recipe.getRecipeId() + ".description", 40);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        IStackHelper stackHelper = helpers.getStackHelper();
        ItemStack recipeOutput = recipe.getRecipeOutput();

        try {
            List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(Arrays.asList(recipe.getInput()));
            ingredients.setInputLists(ItemStack.class, inputs);
            if (recipeOutput != null) {
                ingredients.setOutput(ItemStack.class, recipeOutput);
            }
        } catch (RuntimeException e) {
            ModularRouters.logger.warn("broken recipe: " + recipe.getClass() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (slotIndex == 0 && !input) {
            tooltip.add(TextFormatting.GREEN + TextFormatting.BOLD.toString() + name);
            for (String d : description) {
                tooltip.add(TextFormatting.GREEN + d);
            }
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

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        guiItemStacks.addTooltipCallback(this);

        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        List<List<ItemStack>> outputs = ingredients.getOutputs(ItemStack.class);

        guiItemStacks.set(craftOutputSlot, outputs.get(0).get(0));  // ??

        ICraftingGridHelper craftingGridHelper =
                helpers.getGuiHelper().createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
        craftingGridHelper.setInputs(guiItemStacks, inputs, getWidth(), getHeight());
//        craftingGridHelper.setOutput(guiItemStacks, outputs);
    }
}
