package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActionType;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActivatorSettings;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.EntityMode;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.LookDirection;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import me.desht.modularrouters.test.RouterTestHelper.ModuleSettingsBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

public class ActivatorModuleTest {
    @GameTest
    @TestHolder
    @EmptyTemplate(floor = true)
    static void activatorTestBlock(final RouterTestHelper helper) {
        var router = helper.placeRouter(0, 1, 1, Direction.EAST);
        helper.setBlock(2, 1, 1, Blocks.LEVER);

        ItemStack activator = ModItems.ACTIVATOR_MODULE.toStack();
        activator.set(ModDataComponents.COMMON_MODULE_SETTINGS, ModuleSettingsBuilder.create().facing(RelativeDirection.FRONT).build());
        router.addModule(activator);

        helper.startSequence()
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> helper.assertBlockProperty(new BlockPos(2, 1, 1), LeverBlock.POWERED, true))
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> helper.assertBlockProperty(new BlockPos(2, 1, 1), LeverBlock.POWERED, false))
                .thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(floor = true)
    static void activatorTestItem(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1, Direction.EAST);
        helper.setBlock(2, 1, 1, Blocks.AIR);

        ItemStack activator = ModItems.ACTIVATOR_MODULE.toStack();
        activator.set(ModDataComponents.COMMON_MODULE_SETTINGS, ModuleSettingsBuilder.create().facing(RelativeDirection.FRONT).build());
        activator.set(ModDataComponents.ACTIVATOR_SETTINGS, new ActivatorSettings(
                ActionType.ITEM_OR_BLOCK, LookDirection.BELOW, EntityMode.NEAREST, false)
        );
        router.addModule(activator);
        router.insertBuffer(Items.FLINT_AND_STEEL.getDefaultInstance());

        helper.startSequence()
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.FIRE, 2,1 , 1))
                .thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(floor = true)
    static void activatorTestEntity(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 2, 1, Direction.EAST);
        helper.spawn(EntityType.COW, 2, 2, 1);

        ItemStack activator = ModItems.ACTIVATOR_MODULE.toStack();
        activator.set(ModDataComponents.COMMON_MODULE_SETTINGS, ModuleSettingsBuilder.create().facing(RelativeDirection.FRONT).build());
        activator.set(ModDataComponents.ACTIVATOR_SETTINGS, new ActivatorSettings(
                ActionType.USE_ITEM_ON_ENTITY, LookDirection.LEVEL, EntityMode.NEAREST, false)
        );
        router.addModule(activator);
        router.insertBuffer(Items.BUCKET.getDefaultInstance());

        helper.startSequence()
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> router.assertBuffer(Items.MILK_BUCKET, 1))
                // 4 buckets in router; 3 empty buckets should stay, 1 milk bucket dropped
                .thenExecute(() -> router.setBuffer(new ItemStack(Items.BUCKET, 4)))
                .thenIdle(router.routerTicks(1))
                .thenExecute(() -> router.assertBuffer(Items.BUCKET, 3))
                .thenExecute(() -> helper.assertItemEntityCountIs(Items.MILK_BUCKET, new BlockPos(2, 2, 1), 1.0, 1))
                .thenSucceed();

    }
}
