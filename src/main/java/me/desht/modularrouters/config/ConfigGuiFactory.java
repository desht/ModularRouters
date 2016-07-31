package me.desht.modularrouters.config;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraft) {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return MRConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement runtimeOptionCategoryElement) {
        return null;
    }

    public static class MRConfigGui extends GuiConfig {
        public MRConfigGui(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), ModularRouters.modId,
                    false, false, I18n.format("gui.config.mainTitle"));
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> list = new ArrayList<>();
            list.add(new DummyConfigElement.DummyCategoryElement("mainCfg", "gui.config.ctgy.router", CategoryEntryRouter.class));
            list.add(new DummyConfigElement.DummyCategoryElement("mainCfg", "gui.config.ctgy.module", CategoryEntryModule.class));
            return list;
        }

        public static class CategoryEntryRouter extends GuiConfigEntries.CategoryEntry {
            public CategoryEntryRouter(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
                super(owningScreen, owningEntryList, configElement);
            }

            @Override
            protected GuiScreen buildChildScreen() {
                Configuration configuration = Config.getConfig();
                ConfigElement catRouter = new ConfigElement(configuration.getCategory(Config.CATEGORY_NAME_ROUTER));
                List<IConfigElement> propertiesOnThisScreen = catRouter.getChildElements();
                String windowTitle = configuration.toString();
                return new GuiConfig(this.owningScreen, propertiesOnThisScreen,
                        this.owningScreen.modID,
                        Config.CATEGORY_NAME_ROUTER,
                        this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        windowTitle);
            }
        }

        public static class CategoryEntryModule extends GuiConfigEntries.CategoryEntry {
            public CategoryEntryModule(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
                super(owningScreen, owningEntryList, configElement);
            }

            @Override
            protected GuiScreen buildChildScreen() {
                Configuration configuration = Config.getConfig();
                ConfigElement catModule = new ConfigElement(configuration.getCategory(Config.CATEGORY_NAME_MODULE));
                List<IConfigElement> propertiesOnThisScreen = catModule.getChildElements();
                String windowTitle = configuration.toString();
                return new GuiConfig(this.owningScreen, propertiesOnThisScreen,
                        this.owningScreen.modID,
                        Config.CATEGORY_NAME_MODULE,
                        this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                        this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                        windowTitle);
            }
        }
    }
}
