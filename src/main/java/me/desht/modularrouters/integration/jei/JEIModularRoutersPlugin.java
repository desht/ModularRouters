package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.client.gui.filter.BulkItemFilterScreen;
import me.desht.modularrouters.client.gui.module.ModuleScreen;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import me.desht.modularrouters.util.ItemTagMatcher;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.Tags;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static me.desht.modularrouters.util.MiscUtil.RL;

@JeiPlugin
public class JEIModularRoutersPlugin implements IModPlugin {
    static final Map<IIngredientType<?>, Function<Object, ItemStack>> STACK_CREATORS = new IdentityHashMap<>();

    public static synchronized <T> void registerGhostStackCreator(IIngredientType<T> type, Function<T, ItemStack> creator) {
        STACK_CREATORS.put(type, object -> creator.apply((T) object));
    }

    public JEIModularRoutersPlugin() {
        registerGhostStackCreator(VanillaTypes.ITEM_STACK, Function.identity());
        registerGhostStackCreator(NeoForgeTypes.FLUID_STACK, fs -> fs.getFluidType().getBucket(fs));
    }

    @Override
    public Identifier getPluginUid() {
        return RL("default");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(
                new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, ModularRouters.id("breaker_module")), new ShapelessRecipe(
                        new Recipe.CommonInfo(false),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                        new ItemStackTemplate(ModItems.BREAKER_MODULE.get()),
                        List.of(
                                Ingredient.of(ModItems.BLANK_MODULE.get()),
                                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.PICKAXES))
                        )
                )),
                new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, ModularRouters.id("extruder_module_1")), new ShapelessRecipe(
                        new Recipe.CommonInfo(false),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                        new ItemStackTemplate(ModItems.EXTRUDER_MODULE_1.get()),
                        List.of(
                                Ingredient.of(ModItems.BLANK_MODULE.get()),
                                Ingredient.of(ModItems.BREAKER_MODULE.get()),
                                Ingredient.of(ModItems.PLACER_MODULE.get())
                        )
                ))
        ));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(ModuleScreen.class, new ModuleScreenGhost());
        registration.addGhostIngredientHandler(BulkItemFilterScreen.class, new BulkFilterScreenGhost());

        registration.addGuiContainerHandler(ModularRouterScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(ModularRouterScreen routerScreen) {
                return routerScreen.getExtraArea();
            }
        });
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        for (var item : BuiltInRegistries.ITEM) {
            switch (item) {
                case ModuleItem _ ->
                        registration.addAlias(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), "Modular Router Module");
                case UpgradeItem _ ->
                        registration.addAlias(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), "Modular Router Upgrade");
                case AugmentItem _ ->
                        registration.addAlias(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), "Modular Router Module Augment");
                default -> {
                }
            }
        }
    }
}
