package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.util.MiscUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.asFormattable;

public class SpecialRecipeCategoryExtension implements ICustomCraftingCategoryExtension {
    private final ITextComponent name;
    private final List<ITextComponent> description;
    private final SpecialRecipe recipe;
    private final boolean tooltipExists;

    SpecialRecipeCategoryExtension(SpecialRecipe recipe) {
        this.recipe = recipe;
        String path = recipe.getId().getPath();
        tooltipExists = I18n.hasKey("jei.recipe." + path + ".name");
        this.name = MiscUtil.xlate("jei.recipe." + path + ".name");
        this.description = MiscUtil.wrapStringAsTextComponent("jei.recipe." + path + ".description");
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
                    tooltip.add(asFormattable(name).func_240701_a_(TextFormatting.GREEN, TextFormatting.BOLD));
                    description.stream().map(d -> asFormattable(d).func_240699_a_(TextFormatting.GREEN)).forEach(tooltip::add);
                }
            });
        }
    }
}
