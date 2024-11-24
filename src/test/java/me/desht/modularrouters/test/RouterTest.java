package me.desht.modularrouters.test;

import com.mojang.serialization.Codec;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.NotNull;

public class RouterTest {
    @GameTest
    @TestHolder
    @EmptyTemplate
    static void testRouterSpeed(DynamicTest test, RegistrationHelper reg) {
        var counter = reg.registrar(NeoForgeRegistries.Keys.ATTACHMENT_TYPES)
                .register("counter", () -> AttachmentType.builder(() -> 0)
                        .serialize(Codec.INT).build());

        var module = reg.items().register("test_module", () -> new ModuleItem(ModItems.moduleProps(), (r, s) -> new CompiledModule(r, s) {
            @Override
            public boolean execute(@NotNull ModularRouterBlockEntity router) {
                router.setData(counter, router.getData(counter.get()) + 1);
                return true;
            }

            @Override
            public boolean shouldExecute() {
                return true;
            }
        }) {
            @Override
            public TintColor getItemTint() {
                return TintColor.BLACK;
            }

            @Override
            public int getEnergyCost(ItemStack stack) {
                return 0;
            }
        });

        test.onGameTest(RouterTestHelper.class, helper -> {
            var router = helper.placeRouter(1, 2, 1);
            router.addModule(new ItemStack(module.asItem()));

            helper.startSequence()
                    .thenIdle(40) // 2 router ticks
                    .thenExecute(() -> helper.assertValueEqual(router.router().getData(counter), 2, "counter"))

                    // add 5 speed upgrades which will increase tick rate by 10
                    .thenExecute(() -> router.addUpgrade(ModItems.SPEED_UPGRADE.toStack(5)))
                    .thenIdle(30) // 3 router ticks
                    .thenExecute(() -> helper.assertValueEqual(router.router().getData(counter), 5, "counter"))
                    .thenSucceed();
        });
    }
}
