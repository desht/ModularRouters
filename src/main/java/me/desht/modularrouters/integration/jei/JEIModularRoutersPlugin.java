package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.client.gui.filter.BulkItemFilterScreen;
import me.desht.modularrouters.client.gui.module.ModuleScreen;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidUtil;

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
        registerGhostStackCreator(NeoForgeTypes.FLUID_STACK, FluidUtil::getFilledBucket);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return RL("default");
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
            if (item instanceof ModuleItem) {
                registration.addAlias(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), "Modular Router Module");
            } else if (item instanceof UpgradeItem) {
                registration.addAlias(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), "Modular Router Upgrade");
            } else if (item instanceof AugmentItem) {
                registration.addAlias(VanillaTypes.ITEM_STACK, item.getDefaultInstance(), "Modular Router Module Augment");
            }
        }
    }
}
