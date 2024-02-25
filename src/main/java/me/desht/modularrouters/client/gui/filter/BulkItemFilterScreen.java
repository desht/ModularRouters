package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.FilterOp;
import me.desht.modularrouters.network.messages.FilterSettingsMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class BulkItemFilterScreen extends AbstractFilterContainerScreen {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(ModularRouters.MODID, "textures/gui/bulkitemfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 233;

    private ModuleTarget target;

    public BulkItemFilterScreen(AbstractSmartFilterMenu container, Inventory inventory, Component displayName) {
        super(container, inventory, displayName);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new ClearButton(leftPos + 8, topPos + 130,
                p -> PacketDistributor.SERVER.noArg().send(new FilterSettingsMessage(FilterOp.CLEAR_ALL, menu.getLocator(), null))
        ));

        MFLocator locator = menu.getLocator();
        if (locator.filterSlot() >= 0) {
            // in a module; add a back button to go back to module gui
            addRenderableWidget(new BackButton(leftPos + 2, topPos + 2, p -> closeGUI()));
        }

        if (locator.routerSlot() >= 0 && locator.routerPos() != null) {
            // in a module in a router; add buttons to merge/load the module's target inventory
            ItemStack moduleStack = locator.getModuleStack(Minecraft.getInstance().player);
            ModularRouterBlockEntity router = menu.getRouter();
            CompiledModule cm = ((ModuleItem) moduleStack.getItem()).compile(router, moduleStack);
            target = cm.getEffectiveTarget(router);
            if (target.hasItemHandlerClientSide()) {
                addRenderableWidget(new MergeButton(leftPos + 28, topPos + 130, target.toString(),
                        I18n.get(target.blockTranslationKey), p -> {
                    if (target != null) {
                        PacketDistributor.SERVER.noArg().send(new FilterSettingsMessage(
                                FilterOp.MERGE, menu.getLocator(), target.toNBT()));
                    }
                }));
                addRenderableWidget(new LoadButton(leftPos + 48, topPos + 130, target.toString(),
                        I18n.get(target.blockTranslationKey), p -> {
                    if (target != null) {
                        PacketDistributor.SERVER.noArg().send(new FilterSettingsMessage(
                                FilterOp.LOAD, menu.getLocator(), target.toNBT()));
                    }
                }));
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, this.imageWidth / 2 - font.width(title) / 2, 8, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(TEXTURE_LOCATION, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    static class ClearButton extends Buttons.DeleteButton {
        ClearButton(int x, int y, OnPress pressable) {
            super(x, y, 0, pressable);
            setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip.clearFilter")));
        }
    }

    static class MergeButton extends Buttons.AddButton {
        MergeButton(int x, int y, String locStr, String name, OnPress pressable) {
            super(x, y, pressable);
            setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip.mergeFilter", name, locStr)));
        }
    }

    static class LoadButton extends TexturedButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(144, 16);

        LoadButton(int x, int y, String locStr, String name, OnPress pressable) {
            super( x, y, 16, 16, pressable);
            setTooltip(Tooltip.create(xlate("modularrouters.guiText.tooltip.loadFilter", name, locStr)));
        }

        @Override
        protected XYPoint getTextureXY() {
            return TEXTURE_XY;
        }
    }
}
