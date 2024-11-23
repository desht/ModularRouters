package me.desht.modularrouters.test;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.moddiscovery.locators.UserdevLocator;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.conf.MissingDescriptionAction;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.summary.GitHubActionsStepSummaryDumper;
import org.lwjgl.glfw.GLFW;

@Mod("modularrouterstest")
public class ModularRoutersTest {
    public ModularRoutersTest(ModContainer container, IEventBus eventBus) {
        // we only set up our tests in modular routers test, and not when others use the test helpers
        if (!(container.getModInfo().getOwningFile().getFile().getDiscoveryAttributes().locator() instanceof UserdevLocator)) return;

        final MutableTestFramework framework = FrameworkConfiguration.builder(ResourceLocation.fromNamespaceAndPath(container.getNamespace(), "tests"))
                .clientConfiguration(() -> ClientConfiguration.builder()
                        .toggleOverlayKey(GLFW.GLFW_KEY_J)
                        .openManagerKey(GLFW.GLFW_KEY_N)
                        .build())
                .enable(Feature.CLIENT_SYNC, Feature.CLIENT_MODIFICATIONS)
                .dumpers(new GitHubActionsStepSummaryDumper())
                .build()
                .create();

        framework.init(eventBus, container);
    }
}
