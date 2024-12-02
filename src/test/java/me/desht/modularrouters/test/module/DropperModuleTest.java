package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

public class DropperModuleTest {
    @GameTest
    @TestHolder
    @EmptyTemplate(floor = true)
    static void testDropperModule(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);

        router.addDirectionalModule(ModItems.DROPPER_MODULE, RelativeDirection.UP);

        helper.startSequence()
                .thenExecute(() -> router.insertBuffer(new ItemStack(Items.DIAMOND)))
                .thenIdle(router.routerTicks(1))
                .thenExecute(router::assertBufferEmpty)
                .thenExecute(() -> helper.assertItemEntityPresent(Items.DIAMOND, new BlockPos(1, 2, 1), 0.0))
                .thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(floor = true)
    static void testDropperModuleStacked(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);

        router.addDirectionalModule(ModItems.DROPPER_MODULE, RelativeDirection.FRONT);  // effectively north
        router.addUpgrade(ModItems.STACK_UPGRADE.toStack(5)); // 32 items at a time

        helper.startSequence()
                .thenExecute(() -> router.insertBuffer(new ItemStack(Items.DIAMOND, 64)))
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> router.assertBuffer(Items.DIAMOND, 32))
                .thenExecute(() -> helper.assertItemEntityCountIs(Items.DIAMOND, new BlockPos(1, 1, 0), 0.0, 32))
                .thenSucceed();
    }
}
