package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

public class VacuumModuleTest {
    @GameTest
    @TestHolder
    @EmptyTemplate("13x3x13")
    static void testVacuumModule(final RouterTestHelper helper) {
        var router = helper.placeRouter(6, 1, 6);

        router.addModule(ModItems.VACUUM_MODULE.toStack());

        helper.startSequence()
                .thenExecute(() -> helper.spawnItem(Items.APPLE, 64, 6.5f, 2.5f, 6.5f))
                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> router.assertBuffer(Items.APPLE, 2))

                .thenExecute(() -> router.modifyAugments(0, u -> u.insertItem(0, ModItems.STACK_AUGMENT.toStack(2), false)))
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> router.assertBuffer(Items.APPLE, 6))
                .thenExecute(() -> helper.assertItemEntityCountIs(Items.APPLE, new BlockPos(6, 2, 6), 0, 58))
                .thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(value = "5x3x5", floor = true)
    static void testVacuumModuleRange(final RouterTestHelper helper) {
        var router = helper.placeRouter(2, 2, 2).maxSpeed();

        router.addModule(ModItems.VACUUM_MODULE.toStack());
        // Reduce range to 1 block
        router.modifyAugments(0, u -> u.insertItem(0, ModItems.RANGE_DOWN_AUGMENT.toStack(5), false));

        helper.startSequence()
                .thenExecute(() -> helper.spawnItem(Items.APPLE, 64, 4.5f, 2.5f, 3.5f))
                .thenIdle(router.routerTicks(1))
                .thenExecute(router::assertBufferEmpty) // range does not suffice, so expect it not to have picked up the items

                // make the module direct towards the left so that the AABB moves and includes the items
                .thenExecute(() -> router.modifyModuleSettings(0, s -> s.facing(RelativeDirection.LEFT))) // right is towards the structure block

                .thenIdle(router.routerTicks(2))
                .thenExecute(() -> router.assertBuffer(Items.APPLE, 2))
                .thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(floor = true)
    static void testVacuumModuleXp(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 2, 1).maxSpeed();

        var stack = ModItems.VACUUM_MODULE.toStack();

        // bottle o enchanting per 7 xp
        stack.set(ModDataComponents.VACUUM_SETTINGS, new CompiledVacuumModule.VacuumSettings(false, XPCollection.XPCollectionType.BOTTLE_O_ENCHANTING));
        router.addModule(stack);
        router.modifyAugments(0, u -> {
            // Reduce range to 1 block
            u.insertItem(0, ModItems.RANGE_DOWN_AUGMENT.toStack(5), false);

            // Enable XP collection
            u.insertItem(1, ModItems.XP_VACUUM_AUGMENT.toStack(), false);
        });

        var orb = helper.spawn(EntityType.EXPERIENCE_ORB, 1.5f, 2.5f, 2.5f);
        // not perfectly divisible by 7, but we do have losses when converting so that's intended
        orb.value = 16;
        orb.count = 4;
        helper.startSequence()
                .thenIdle(router.routerTicks(3))

                .thenExecute(() -> router.assertBuffer(Items.EXPERIENCE_BOTTLE, 6)) // 3 orbs * 2 bottles (14 xp value)
                .thenExecute(() -> helper.assertValueEqual(orb.count, 1, "orb count")) // should have decreased the count of the orb by 3
                .thenExecute(() -> helper.assertValueEqual(orb.value, 16, "orb value")) // but don't touch the value
                .thenSucceed();
    }
}
