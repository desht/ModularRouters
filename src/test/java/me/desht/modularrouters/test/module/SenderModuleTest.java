package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

public class SenderModuleTest {
    @GameTest
    @TestHolder
    @EmptyTemplate
    static void testSenderMk1(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var chest = helper.placeChest(1, 2, 1);

        router.insertBuffer(new ItemStack(Items.APPLE, 64));
        helper.addDirectionalModule(router, ModItems.SENDER_MODULE_1, RelativeDirection.UP);

        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 1, "chest contents"))
                .thenIdle(40)
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 3, "chest contents"))
                .thenExecute(() -> helper.assertValueEqual(router.getBufferItemStack().getCount(), 61, "router contents"))
                .thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate("5x5x5")
    static void testSenderMk2(final RouterTestHelper helper) {
        var router = helper.placeRouter(3, 4, 3);
        var chest = helper.placeChest(0, 1, 0);

        router.insertBuffer(new ItemStack(Items.APPLE, 64));
        helper.addTargetedModule(router, ModItems.SENDER_MODULE_2, 0, 1, 0, Direction.UP);

        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 1, "chest contents"))
                .thenIdle(40)
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 3, "chest contents"))
                .thenExecute(() -> helper.assertValueEqual(router.getBufferItemStack().getCount(), 61, "router contents"))

                .thenSucceed();
    }
}
