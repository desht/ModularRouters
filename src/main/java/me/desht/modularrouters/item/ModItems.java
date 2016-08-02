package me.desht.modularrouters.item;

import me.desht.modularrouters.item.module.*;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static ItemBase blankModule;
    public static ItemModule module;
    public static ItemBase blankUpgrade;
    public static ItemUpgrade upgrade;

    public static void init() {
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
