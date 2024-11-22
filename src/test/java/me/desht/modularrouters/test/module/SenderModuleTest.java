package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedSequence;

public class SenderModuleTest {
    @TestHolder
    @EmptyTemplate
    @GameTest(timeoutTicks = 1000)
    static void testSenderMk1(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var chest = helper.placeChest(1, 2, 1);

        router.addDirectionalModule(ModItems.SENDER_MODULE_1, RelativeDirection.UP);

        testTransfer(helper.startSequence(), helper, router, chest).thenSucceed();
    }

    @TestHolder
    @EmptyTemplate("5x5x5")
    @GameTest(timeoutTicks = 1000)
    static void testSenderMk2(final RouterTestHelper helper) {
        var router = helper.placeRouter(3, 4, 3);
        var chest = helper.placeChest(0, 1, 0);

        router.addTargetedModule(ModItems.SENDER_MODULE_2, 0, 1, 0, Direction.UP);

        testTransfer(helper.startSequence(), helper, router, chest).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate("5x5x5")
    static void testSenderMk2Range(final RouterTestHelper helper) {
        var router = helper.placeRouter(3, 4, 3);
        helper.placeChest(0, 1, 0);

        router.addTargetedModule(ModItems.SENDER_MODULE_2, 0, 1, 0, Direction.UP);
        // reduce the range to 2 blocks which is not enough
        router.modifyAugments(0, h -> h.insertItem(0, ModItems.RANGE_DOWN_AUGMENT.toStack(22), false));
        router.insertBuffer(new ItemStack(Items.APPLE, 64));

        // make sure we haven't sent anything
        helper.startSequence()
                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> helper.assertContainerEmpty(0, 1, 0))
                .thenExecute(() -> helper.assertValueEqual(router.getBuffer().getCount(), 64, "chest contents"))
                .thenSucceed();
    }

    private static ExtendedSequence testTransfer(ExtendedSequence seq, RouterTestHelper helper, RouterTestHelper.RouterWrapper router, ChestBlockEntity chest) {
        return seq
                .thenExecute(() -> router.insertBuffer(new ItemStack(Items.APPLE, 64)))

                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 1, "chest contents"))
                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 3, "chest contents"))

                // 2 stack upgrades - 4 items / tick
                .thenExecute(() -> router.addUpgrade(ModItems.STACK_UPGRADE.toStack(2)))
                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 11, "chest contents"))

                // 4 stack augments - 16 items / tick (stack upgrades are ignored)
                .thenExecute(() -> router.modifyAugments(0, aug -> aug.insertItem(0, ModItems.STACK_AUGMENT.toStack(4), false)))
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> helper.assertValueEqual(chest.countItem(Items.APPLE), 27, "chest contents"))

                .thenExecute(() -> helper.assertValueEqual(router.getBuffer().getCount(), 37, "router contents"))

                .thenExecute(router::clearBuffer);
    }
}
