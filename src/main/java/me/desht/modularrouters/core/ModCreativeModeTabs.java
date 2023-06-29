package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import me.desht.modularrouters.recipe.GuideBookRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularRouters.MODID);

    public static final RegistryObject<CreativeModeTab> DEFAULT = TABS.register("default", ModCreativeModeTabs::buildDefaultTab);

    private static CreativeModeTab buildDefaultTab() {
        List<ItemStack> items = ModItems.ITEMS.getEntries().stream()
                .map(ro -> new ItemStack(ro.get()))
                .sorted(new ItemSorter())
                .collect(Collectors.toCollection(ArrayList::new));

        if (ModList.get().isLoaded("patchouli")) {
            items.add(GuideBookRecipe.makeGuideBook());
        }

        return CreativeModeTab.builder()
                .title(Component.literal(ModularRouters.MODNAME))
                .icon(() -> new ItemStack(ModBlocks.MODULAR_ROUTER.get()))
                .displayItems((params, output) -> output.acceptAll(items))
                .build();
    }

    private static class ItemSorter implements Comparator<ItemStack> {
        @Override
        public int compare(ItemStack s1, ItemStack s2) {
            for (Class<?> cls : List.of(BlockItem.class, ModuleItem.class, UpgradeItem.class, AugmentItem.class, SmartFilterItem.class)) {
                if (cls.isAssignableFrom(s1.getItem().getClass()) && !cls.isAssignableFrom(s2.getItem().getClass())) {
                    return -1;
                } else if (cls.isAssignableFrom(s2.getItem().getClass()) && !cls.isAssignableFrom(s1.getItem().getClass())) {
                    return 1;
                }
            }
            return s1.getDisplayName().getString().compareTo(s2.getDisplayName().getString());
        }
    }
}
