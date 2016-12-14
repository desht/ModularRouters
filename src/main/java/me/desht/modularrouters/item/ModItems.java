package me.desht.modularrouters.item;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
    public static ItemBase blankModule;
    public static ItemModule module;
    public static ItemBase blankUpgrade;
    public static ItemUpgrade upgrade;
    public static ItemBase overrideCard;
    public static ItemSmartFilter smartFilter;

    public static void init() {
        blankModule = register(new ItemBase("blank_module"));
        module = register(new ItemModule(), ItemModule.SUBTYPES);
        blankUpgrade = register(new ItemBase("blank_upgrade"));
        upgrade = register(new ItemUpgrade(), ItemUpgrade.SUBTYPES);
        overrideCard = register(new ItemBase("override_card"));
        smartFilter = register(new ItemSmartFilter(), ItemSmartFilter.SUBTYPES);
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
