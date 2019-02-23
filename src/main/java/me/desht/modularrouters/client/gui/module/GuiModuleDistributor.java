package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule.DistributionStrategy;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.List;

public class GuiModuleDistributor extends GuiModule {
    private static final int STRATEGY_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;
    private static final int TOOLTIP_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 1;

    private DistributionStrategy strategy;

    public GuiModuleDistributor(ContainerModule container) {
        super(container);

        CompiledDistributorModule cdm = new CompiledDistributorModule(null, moduleItemStack);

        strategy = cdm.getDistributionStrategy();
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(new TooltipButton(TOOLTIP_BUTTON_ID, guiLeft + 130, guiTop + 23));
        addButton(new StrategyButton(STRATEGY_BUTTON_ID, guiLeft + 147, guiTop + 23, 16, 16, strategy) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                strategy = cycle(!GuiScreen.isShiftKeyDown());
                sendModuleSettingsToServer();
            }
        });

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 21, guiLeft + 165, guiTop + 41, "guiText.popup.distributor.strategy");
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound tag = super.buildMessageData();
        tag.putInt(CompiledDistributorModule.NBT_STRATEGY, strategy.ordinal());
        return tag;
    }

    private class StrategyButton extends TexturedCyclerButton<DistributionStrategy> {
        private final List<List<String>> tooltips = Lists.newArrayList();

        StrategyButton(int buttonId, int x, int y, int width, int height, DistributionStrategy initialVal) {
            super(buttonId, x, y, width, height, initialVal);
            for (DistributionStrategy strategy : DistributionStrategy.values()) {
                tooltips.add(Collections.singletonList(I18n.format("itemText.distributor.strategy." + strategy)));
            }
        }

        @Override
        protected int getTextureX() {
            return 160 + getState().ordinal() * 16;
        }

        @Override
        protected int getTextureY() {
            return 32;
        }

        @Override
        public List<String> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }

    private class TooltipButton extends TexturedButton {
        TooltipButton(int id, int x, int y) {
            super(id, x, y, 16, 16);
            tooltip1.add(I18n.format("guiText.tooltip.distributor.strategy"));
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
        }

        @Override
        protected int getTextureX() {
            return 176;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }
}
