package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule.DistributionStrategy;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class DistributorModuleScreen extends AbstractModuleScreen {
    private static final ItemStack ROUTER_STACK = new ItemStack(ModBlocks.MODULAR_ROUTER.get());

    private StrategyButton sb;
    private DirectionButton db;

    public DistributorModuleScreen(ModuleMenu container, Inventory inv, Component displayText) {
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
                xlate("modularrouters.guiText.popup.distributor.strategy").withStyle(ChatFormatting.YELLOW));
        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 41, leftPos + 165, topPos + 61,
                xlate("modularrouters.guiText.popup.distributor.direction").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    protected CompoundTag buildMessageData() {
        return Util.make(super.buildMessageData(), tag -> {
            tag.putInt(CompiledDistributorModule.NBT_STRATEGY, sb.getState().ordinal());
            tag.putBoolean(CompiledDistributorModule.NBT_PULLING, db.isToggled());
        });
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        graphics.renderItem(ROUTER_STACK, leftPos + 127, topPos + 43);
    }

    private class StrategyButton extends TexturedCyclerButton<DistributionStrategy> {
        StrategyButton(int x, int y, int width, int height, DistributionStrategy initialVal) {
            super(x, y, width, height, initialVal, DistributorModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(160 + getState().ordinal() * 16, 32);
        }
    }

    private class DirectionButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(176, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(160, 16);

        public DirectionButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, DistributorModuleScreen.this);

            setTooltips(xlate("modularrouters.itemText.fluid.direction.OUT"),xlate("modularrouters.itemText.fluid.direction.IN"));
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }

    private static class TooltipButton extends ItemStackButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(176, 16);

        TooltipButton(int x, int y) {
            super(x, y, 16, 16, new ItemStack(ModItems.DISTRIBUTOR_MODULE.get()), true, p -> {});
            setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip.distributor.strategy")));
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
        }

        @Override
        protected XYPoint getTextureXY() {
            return TEXTURE_XY;
        }
    }
}
