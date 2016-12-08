package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.ModularRouters;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class ModuleEnhancementRecipeCategory extends BlankRecipeCategory {
    private static final int craftOutputSlot = 0;
    private static final int craftInputSlot1 = 1;
    private static final ResourceLocation jeiResource = new ResourceLocation(ModularRouters.modId, "textures/jei/moduleEnhancement.png");
    public static final int GUI_WIDTH = 168;

    private final ICraftingGridHelper craftingGridHelper;
    private final String title;
    private final IDrawable background;
    private final IDrawableStatic icon;

    public ModuleEnhancementRecipeCategory(IGuiHelper guiHelper) {
        title = I18n.format("jei.enhancement.title");
        background = guiHelper.createDrawable(jeiResource, 0, 0, GUI_WIDTH, 84);
        icon = guiHelper.createDrawable(jeiResource, 176, 0, 16, 16);
        craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
    }

    @Override
    public String getUid() {
        return ModularRouters.modId + ".moduleEnhancement";
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.addTooltipCallback((ModuleEnhancementRecipeWrapper) recipeWrapper);

        // 10 slots: 1-9 are inputs, 0 is output (normal 3x3 crafting grid layout)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                guiItemStacks.init(i + j * 3 + craftInputSlot1, true, 23 + 18 * i, 6 + 18 * j);
            }
        }
        guiItemStacks.init(craftOutputSlot, false, 119, 24);

        // doesn't seem to do anything!
        recipeLayout.setRecipeTransferButton(143, 48);

        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
        List<ItemStack> outputs = ingredients.getOutputs(ItemStack.class);

        IShapedCraftingRecipeWrapper wrapper = (IShapedCraftingRecipeWrapper) recipeWrapper;
        craftingGridHelper.setInputStacks(guiItemStacks, inputs, wrapper.getWidth(), wrapper.getHeight());
        craftingGridHelper.setOutput(guiItemStacks, outputs);
    }
}
