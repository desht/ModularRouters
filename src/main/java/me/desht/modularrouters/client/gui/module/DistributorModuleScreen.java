package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule.DistributionStrategy;
import me.desht.modularrouters.logic.settings.TransferDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class DistributorModuleScreen extends ModuleScreen {
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

        addRenderableWidget(new TooltipButton(leftPos + 127, topPos + 23, ModItems.DISTRIBUTOR_MODULE.toStack()));
        addRenderableWidget(sb = new StrategyButton(leftPos + 147, topPos + 23, 16, 16, cdm.getDistributionStrategy(), this));
        addRenderableWidget(db = new DirectionButton(leftPos + 147, topPos + 43, cdm.isPulling(), this));

        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 21, leftPos + 165, topPos + 41,
                xlate("modularrouters.guiText.popup.distributor.strategy").withStyle(ChatFormatting.YELLOW));
        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 41, leftPos + 165, topPos + 61,
                xlate("modularrouters.guiText.popup.distributor.direction").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    protected void buildComponentPatch(DataComponentPatch.Builder builder) {
        super.buildComponentPatch(builder);
        builder.set(ModDataComponents.DISTRIBUTOR_SETTINGS.get(), new CompiledDistributorModule.DistributorSettings(
                        sb.getState(),
                        db.isToggled() ? TransferDirection.TO_ROUTER : TransferDirection.FROM_ROUTER
                )
        );
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        graphics.item(ROUTER_STACK, leftPos + 127, topPos + 43);
    }

    static class StrategyButton extends TexturedCyclerButton<DistributionStrategy> {
        StrategyButton(int x, int y, int width, int height, DistributionStrategy initialVal, ISendToServer sendToServer) {
            super(x, y, width, height, initialVal, sendToServer);
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(160 + getState().ordinal() * 16, 32);
        }
    }

    static class DirectionButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(176, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(160, 16);

        public DirectionButton(int x, int y, boolean initialVal, ISendToServer sender) {
            super(x, y, 16, 16, initialVal, sender);

            setTooltips(xlate("modularrouters.itemText.transfer_direction.from_router"),xlate("modularrouters.itemText.transfer_direction.to_router"));
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }

    static class TooltipButton extends ItemStackButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(176, 16);

        TooltipButton(int x, int y, ItemStack stack) {
            super(x, y, 16, 16, stack, true, p -> {});
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
