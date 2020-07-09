package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule.DistributionStrategy;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

public class GuiModuleDistributor extends GuiModule {
    private StrategyButton sb;

    public GuiModuleDistributor(ContainerModule container, PlayerInventory inv, ITextComponent displayText) {
        super(container, inv, displayText);
    }

    @Override
    public void init() {
        super.init();

        CompiledDistributorModule cdm = new CompiledDistributorModule(null, moduleItemStack);

        addButton(new TooltipButton(guiLeft + 130, guiTop + 23));
        addButton(sb = new StrategyButton(guiLeft + 147, guiTop + 23, 16, 16, cdm.getDistributionStrategy()));

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 21, guiLeft + 165, guiTop + 41,
                "guiText.popup.distributor.strategy");
    }

    @Override
    protected CompoundNBT buildMessageData() {
        CompoundNBT tag = super.buildMessageData();
        tag.putInt(CompiledDistributorModule.NBT_STRATEGY, sb.getState().ordinal());
        return tag;
    }

    private class StrategyButton extends TexturedCyclerButton<DistributionStrategy> {
        private final List<List<ITextComponent>> tooltips = Lists.newArrayList();

        StrategyButton(int x, int y, int width, int height, DistributionStrategy initialVal) {
            super(x, y, width, height, initialVal, GuiModuleDistributor.this);
            for (DistributionStrategy strategy : DistributionStrategy.values()) {
                tooltips.add(Collections.singletonList(ClientUtil.xlate(strategy.getTranslationKey())));
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
        public List<ITextComponent> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }

    private static class TooltipButton extends TexturedButton {
        TooltipButton(int x, int y) {
            super(x, y, 16, 16, p -> {});
            tooltip1.add(ClientUtil.xlate("guiText.tooltip.distributor.strategy"));
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        public void playDownSound(SoundHandler soundHandlerIn) {
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
