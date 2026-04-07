package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.messages.BulkFilterUpdateMessage;
import me.desht.modularrouters.network.messages.BulkFilterUpdateMessage.FilterOp;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.function.Consumer;

import static me.desht.modularrouters.client.util.ClientUtil.*;

public class BulkItemFilterScreen extends AbstractFilterContainerScreen {
    private static final Identifier TEXTURE_LOCATION = MiscUtil.RL("textures/gui/bulkitemfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 233;

    public BulkItemFilterScreen(AbstractSmartFilterMenu container, Inventory inventory, Component displayName) {
        super(container, inventory, displayName, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new ClearButton(leftPos + 8, topPos + 130,
                _ -> ClientPacketDistributor.sendToServer(BulkFilterUpdateMessage.untargeted(FilterOp.CLEAR_ALL, menu.getLocator()))
        ));

        MFLocator locator = menu.getLocator();
        if (locator.filterSlot() >= 0) {
            // in a module; add a back button to go back to module gui
            addRenderableWidget(new BackButton(leftPos + 2, topPos + 2, _ -> closeGUI()));
        }

        locator.getRouter(getClientLevel()).ifPresent(router -> {
            // in a module in a router; add buttons to merge/load the module's target inventory
            ItemStack moduleStack = locator.getModuleStack(getClientPlayer());
            CompiledModule cm = ((ModuleItem) moduleStack.getItem()).compile(router, moduleStack);
            cm.getEffectiveTarget(router).ifPresent(target -> {
                if (target.hasItemHandler(getClientLevel())) {
                    MutableComponent title = xlate(target.blockTranslationKey);
                    addRenderableWidget(new MergeButton(leftPos + 28, topPos + 130, target.toString(), title, _ ->
                            ClientPacketDistributor.sendToServer(BulkFilterUpdateMessage.targeted(FilterOp.MERGE, menu.getLocator(), target))));
                    addRenderableWidget(new LoadButton(leftPos + 48, topPos + 130, target.toString(), title, _ ->
                            ClientPacketDistributor.sendToServer(BulkFilterUpdateMessage.targeted(FilterOp.LOAD, menu.getLocator(), target))));
                }
            });
        });
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, this.imageWidth / 2 - font.width(title) / 2, 8, 0x404040, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
    }

    static class ClearButton extends Buttons.DeleteButton {
        ClearButton(int x, int y, Consumer<Buttons.DeleteButton> pressable) {
            super(x, y, 0, pressable);
            setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip.clearFilter")));
        }
    }

    static class MergeButton extends Buttons.AddButton {
        MergeButton(int x, int y, String locStr, Component name, OnPress pressable) {
            super(x, y, pressable);
            setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip.mergeFilter", name.copy().withStyle(ChatFormatting.YELLOW), locStr)));
        }
    }

    static class LoadButton extends TexturedButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(144, 16);

        LoadButton(int x, int y, String locStr, Component name, OnPress pressable) {
            super( x, y, 16, 16, pressable);
            setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip.loadFilter", name.copy().withStyle(ChatFormatting.YELLOW), locStr)));
        }

        @Override
        protected XYPoint getTextureXY() {
            return TEXTURE_XY;
        }
    }
}
