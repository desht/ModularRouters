package me.desht.modularrouters.item;

import me.desht.modularrouters.item.module.*;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    //    public static ItemReceiverModule receiverModule;
//    public static ItemPullerModule pullerModule;
//    public static ItemSenderModule1 senderModule1;
//    public static ItemSenderModule2 senderModule2;
//    public static ItemSenderModule3 senderModule3;
//    public static ItemSorterModule sorterModule;
//    public static ItemDropperModule dropperModule;
//    public static ItemPlacerModule placerModule;
//    public static ItemBreakerModule breakerModule;
//    public static ItemVacuumModule vacuumModule;
    public static ItemBase blankModule;
    public static ItemModule module;
    public static ItemBase blankUpgrade;
    public static ItemUpgrade upgrade;

    public static void init() {
//        breakerModule = register(new ItemBreakerModule());
//        dropperModule = register(new ItemDropperModule());
//        pullerModule = register(new ItemPullerModule());
//        receiverModule = register(new ItemReceiverModule());
//        placerModule = register(new ItemPlacerModule());
//        senderModule1 = register(new ItemSenderModule1());
//        senderModule2 = register(new ItemSenderModule2());
//        senderModule3 = register(new ItemSenderModule3());
//        sorterModule = register(new ItemSorterModule());
//        vacuumModule = register(new ItemVacuumModule());
        blankModule = register(new ItemBase("blankModule"));
        module = register(new ItemModule(), ItemModule.SUBTYPES);
        blankUpgrade = register(new ItemBase("blankUpgrade"));
        upgrade = register(new ItemUpgrade(), ItemUpgrade.SUBTYPES);
    }

    private static <T extends Item> T register(T item) {
        return register(item, 0);
    }

    private static <T extends Item> T register(T item, int nSubtypes) {
        GameRegistry.register(item);

        if (item instanceof ItemBase) {
            ((ItemBase) item).registerItemModel(nSubtypes);
        }

        return item;
    }
}
