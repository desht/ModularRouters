package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.util.MiscUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

public class SpecialRecipeCategoryExtension implements ICustomCraftingCategoryExtension {
    private final String name;
    private final List<String> description;
    @javax.annotation.Nonnull
    private final SpecialRecipe recipe;
    private final boolean tooltipExists;

    SpecialRecipeCategoryExtension(SpecialRecipe recipe) {
        this.recipe = recipe;
        String path = recipe.getId().getPath();
        tooltipExists = I18n.hasKey("jei.recipe." + path + ".name");
        this.name = I18n.format("jei.recipe." + path + ".name");
        this.description = MiscUtil.wrapString("jei.recipe." + path + ".description");
    }

    @Override
    public void setIngredients(IIngredients ingredients) {
        ingredients.setInputIngredients(this.recipe.getIngredients());
        ingredients.setOutput(VanillaTypes.ITEM, this.recipe.getRecipeOutput());
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getId();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        if (tooltipExists) {
            layout.getItemStacks().addTooltipCallback((index, input, stack, tooltip) -> {
                if (index == 0 && !input) {
                    tooltip.add(TextFormatting.GREEN + TextFormatting.BOLD.toString() + name);
                    for (String d : description) {
                        tooltip.add(TextFormatting.GREEN + d);
                    }
                }
            });
        }
    }
}
