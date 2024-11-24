package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.logic.settings.TransferDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameType;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

public class PlayerModuleTest {
    @GameTest
    @TestHolder
    @EmptyTemplate(floor = true)
    static void testPlayerModule(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 2, 1).maxSpeed();
        var player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);

        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.APPLE, 10));
        var module = ModItems.PLAYER_MODULE.toStack();
        module.set(ModDataComponents.OWNER, new ResolvableProfile(player.getGameProfile()));
        module.set(ModDataComponents.PLAYER_SETTINGS, new CompiledPlayerModule.PlayerSettings(TransferDirection.TO_ROUTER, CompiledPlayerModule.Section.OFFHAND));
        router.addModule(module);

        helper.startSequence()
                .thenIdle(router.routerTicks(3))
                .thenExecute(() -> router.assertBuffer(Items.APPLE, 3))
                .thenExecute(() -> helper.assertStack(player.getItemInHand(InteractionHand.OFF_HAND), Items.APPLE, 7))

                .thenExecute(() -> router.setBuffer(new ItemStack(Items.DIAMOND_CHESTPLATE)))
                .thenExecute(() -> router.modifyModule(0, s -> s.set(ModDataComponents.PLAYER_SETTINGS,
                        new CompiledPlayerModule.PlayerSettings(TransferDirection.FROM_ROUTER, CompiledPlayerModule.Section.ARMOR))))

                .thenIdle(router.routerTicks(1))

                .thenExecute(router::assertBufferEmpty)
                .thenExecute(() -> helper.assertStack(player.getItemBySlot(EquipmentSlot.CHEST), Items.DIAMOND_CHESTPLATE, 1))
                .thenSucceed();
    }
}
