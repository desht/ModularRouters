package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.settings.*;
import me.desht.modularrouters.test.RouterTestHelper;
import me.desht.modularrouters.test.RouterTestHelper.ModuleSettingsBuilder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.List;

public class FilterTest {
    @GameTest
    @TestHolder
    @EmptyTemplate
    static void simpleFilterTest(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);

        var topChest = helper.placeChest(1, 2, 1); // above router
        var northChest = helper.placeChest(1, 1, 0); // north of router

        // first module blacklists cobblestone
        ItemStack sender1 = ModItems.SENDER_MODULE_1.toStack();
        sender1.set(ModDataComponents.FILTER, ItemContainerContents.fromItems(List.of(Items.COBBLESTONE.getDefaultInstance())));
        sender1.set(ModDataComponents.COMMON_MODULE_SETTINGS, ModuleSettingsBuilder.create().facing(RelativeDirection.UP).build());
        router.router().getModules().insertItem(0, sender1, false);

        // second module has empty blacklist
        ItemStack sender2 = ModItems.SENDER_MODULE_1.toStack();
        sender2.set(ModDataComponents.COMMON_MODULE_SETTINGS, ModuleSettingsBuilder.create().facing(RelativeDirection.FRONT).build());
        router.router().getModules().insertItem(1, sender2, false);

        router.insertBuffer(new ItemStack(Items.COBBLESTONE, 2));

        helper.startSequence()
                .thenIdle(router.routerTicks(2))
                .thenExecute(router::assertBufferEmpty)
                .thenExecute(() -> helper.assertValueEqual(topChest.countItem(Items.COBBLESTONE), 0, "top chest cobblestone"))
                .thenExecute(() -> helper.assertValueEqual(northChest.countItem(Items.COBBLESTONE), 2, "north chest cobblestone"))
                .thenExecute(() -> router.insertBuffer(new ItemStack(Items.IRON_INGOT, 2)))
                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> helper.assertValueEqual(topChest.countItem(Items.IRON_INGOT), 1, "top chest iron"))
                .thenExecute(() -> helper.assertValueEqual(topChest.countItem(Items.IRON_INGOT), 1, "north chest iron"))
                .thenSucceed();
    }
}
