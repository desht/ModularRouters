package me.desht.modularrouters.test.module;

import me.desht.modularrouters.test.RouterTestHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

public class OptionalMekanismTest {
    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalModulesRequireMekanism(final RouterTestHelper helper) {
        for (String name : new String[]{"chemical_module", "chemical_module_2"}) {
            helper.assertValueEqual(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("modularrouters", name)),
                    ModList.get().isLoaded("mekanism"), "optional chemical module registration");
            helper.assertValueEqual(helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("modularrouters", name)).isPresent(),
                    ModList.get().isLoaded("mekanism"), "optional chemical module recipe");
        }
        helper.succeed();
    }
}
