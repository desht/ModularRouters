package me.desht.modularrouters.integration.jei;

// todo 1.13
public class ModuleEnhancementRecipeWrapper
        /*implements ICustomCraftingRecipeWrapper, IShapedCraftingRecipeWrapper, ITooltipCallback<ItemStack>*/ {
//    private static final int craftOutputSlot = 0;
//    private static final int craftInputSlot1 = 1;
//
//    private final IJeiHelpers helpers;
//    private final ModuleEnhancementRecipe recipe;
//    private final String name;
//    private final List<String> description;
//
//    ModuleEnhancementRecipeWrapper(IJeiHelpers helpers, ModuleEnhancementRecipe recipe) {
//        this.helpers = helpers;
//        this.recipe = recipe;
//        this.name = I18n.format("jei.recipe." + recipe.getRecipeId() + ".name");
//        this.description = MiscUtil.wrapString(I18n.format("jei.recipe." + recipe.getRecipeId() + ".description"));
//    }
//
//    @Override
//    public void getIngredients(@Nonnull IIngredients ingredients) {
//        IStackHelper stackHelper = helpers.getStackHelper();
//        ItemStack recipeOutput = recipe.getRecipeOutput();
//
//        try {
//            List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(recipe.getIngredients());
//            ingredients.setInputLists(ItemStack.class, inputs);
//            ingredients.setOutput(ItemStack.class, recipeOutput);
//        } catch (RuntimeException e) {
//            ModularRouters.LOGGER.warn("broken recipe: " + recipe.getClass() + " - " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onTooltip(int slotIndex, boolean input, @Nonnull ItemStack ingredient, @Nonnull List<String> tooltip) {
//        if (slotIndex == 0 && !input) {
//            tooltip.add(TextFormatting.GREEN + TextFormatting.BOLD.toString() + name);
//            for (String d : description) {
//                tooltip.add(TextFormatting.GREEN + d);
//            }
//        }
//    }
//
//    @Override
//    public int getWidth() {
//        return 3;
//    }
//
//    @Override
//    public int getHeight() {
//        return 3;
//    }
//
//    @Override
//    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IIngredients ingredients) {
//        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
//        guiItemStacks.addTooltipCallback(this);
//
//        List<List<ItemStack>> inputs = ingredients.getInputs(ItemStack.class);
//        List<List<ItemStack>> outputs = ingredients.getOutputs(ItemStack.class);
//
//        guiItemStacks.set(craftOutputSlot, outputs.get(0).get(0));  // ??
//
//        ICraftingGridHelper craftingGridHelper =
//                helpers.getGuiHelper().createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
//        craftingGridHelper.setInputs(guiItemStacks, inputs, getWidth(), getHeight());
//    }
}
