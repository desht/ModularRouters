package me.desht.modularrouters.event;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.RL;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabEventHandler {
    @SubscribeEvent
    public static void onCreativeTabRegister(CreativeModeTabEvent.Register event) {
        List<ItemStack> items = ModItems.ITEMS.getEntries().stream()
                .map(ro -> new ItemStack(ro.get()))
                .sorted(new ItemSorter())
                .toList();

        event.registerCreativeModeTab(RL("default"), builder ->
            builder.title(Component.literal(ModularRouters.MODNAME))
                    .icon(() -> new ItemStack(ModBlocks.MODULAR_ROUTER.get()))
                    .displayItems((flags, output, b) -> output.acceptAll(items))
                    .build()
        );
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
