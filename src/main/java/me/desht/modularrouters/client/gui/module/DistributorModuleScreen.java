package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule.DistributionStrategy;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class DistributorModuleScreen extends AbstractModuleScreen {
    private static final ItemStack ROUTER_STACK = new ItemStack(ModBlocks.MODULAR_ROUTER.get());

    private StrategyButton sb;
    private DirectionButton db;

    public DistributorModuleScreen(ContainerModule container, Inventory inv, Component displayText) {
        super(container, inv, displayText);
    }

    @Override
    public void init() {
        super.init();

        CompiledDistributorModule cdm = new CompiledDistributorModule(null, moduleItemStack);

        addRenderableWidget(new TooltipButton(leftPos + 127, topPos + 23));
        addRenderableWidget(sb = new StrategyButton(leftPos + 147, topPos + 23, 16, 16, cdm.getDistributionStrategy()));
        addRenderableWidget(db = new DirectionButton(leftPos + 147, topPos + 43, cdm.isPulling()));

        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 21, leftPos + 165, topPos + 41,
                "modularrouters.guiText.popup.distributor.strategy");
        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 41, leftPos + 165, topPos + 61,
                "modularrouters.guiText.popup.distributor.direction");
    }

    @Override
    protected CompoundTag buildMessageData() {
        CompoundTag tag = super.buildMessageData();
        tag.putInt(CompiledDistributorModule.NBT_STRATEGY, sb.getState().ordinal());
        tag.putBoolean(CompiledDistributorModule.NBT_PULLING, db.isToggled());
        return tag;
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        GuiUtil.renderItemStack(matrixStack, minecraft, ROUTER_STACK, leftPos + 127, topPos + 43, "");
    }

    private class StrategyButton extends TexturedCyclerButton<DistributionStrategy> {
        private final List<List<Component>> tooltips = Lists.newArrayList();

        StrategyButton(int x, int y, int width, int height, DistributionStrategy initialVal) {
            super(x, y, width, height, initialVal, DistributorModuleScreen.this);
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
        public List<Component> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }

    private class DirectionButton extends TexturedToggleButton {
        public DirectionButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, DistributorModuleScreen.this);

            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.itemText.fluid.direction.OUT");
            MiscUtil.appendMultilineText(tooltip2, ChatFormatting.WHITE, "modularrouters.itemText.fluid.direction.IN");
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 160 : 176;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y) {
            super(x, y, 16, 16, new ItemStack(ModItems.DISTRIBUTOR_MODULE.get()), true, p -> {});
            tooltip1.add(ClientUtil.xlate("modularrouters.guiText.tooltip.distributor.strategy"));
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
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
