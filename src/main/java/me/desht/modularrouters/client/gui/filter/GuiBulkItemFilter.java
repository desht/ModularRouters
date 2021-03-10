package me.desht.modularrouters.client.gui.filter;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.FilterSettingsMessage.Operation;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import net.minecraft.client.gui.widget.button.Button.IPressable;

public class GuiBulkItemFilter extends GuiFilterContainer {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/bulkitemfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 233;

    private ModuleTarget target;

    public GuiBulkItemFilter(ContainerSmartFilter container, PlayerInventory inventory, ITextComponent displayName) {
        super(container, inventory, displayName);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        addButton(new ClearButton(leftPos + 8, topPos + 130,
                p -> PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.CLEAR_ALL, menu.getLocator(), null))
        ));

        MFLocator locator = menu.getLocator();
        if (locator.filterSlot >= 0) {
            // in a module; add a back button to go back to module gui
            addButton(new BackButton(leftPos + 2, topPos + 2, p -> closeGUI()));
        }

        if (locator.routerSlot >= 0 && locator.routerPos != null) {
            // in a module in a router; add buttons to merge/load the module's target inventory
            ItemStack moduleStack = locator.getModuleStack(Minecraft.getInstance().player);
            TileEntityItemRouter router = menu.getRouter();
            CompiledModule cm = ((ItemModule) moduleStack.getItem()).compile(router, moduleStack);
            target = cm.getEffectiveTarget(router);
            if (target.getItemHandler(minecraft.level).isPresent()) {
                addButton(new MergeButton(leftPos + 28, topPos + 130, target.toString(),
                        I18n.get(target.blockTranslationKey), p -> {
                    if (target != null) {
                        PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                                Operation.MERGE, menu.getLocator(), target.toNBT()));
                    }
                }));
                addButton(new LoadButton(leftPos + 48, topPos + 130, target.toString(),
                        I18n.get(target.blockTranslationKey), p -> {
                    if (target != null) {
                        PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                                Operation.LOAD, menu.getLocator(), target.toNBT()));
                    }
                }));
            }
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        font.draw(matrixStack, title, this.imageWidth / 2f - font.width(title) / 2f, 8, 0x404040);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        minecraft.getTextureManager().bind(textureLocation);
        blit(matrixStack, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static class ClearButton extends Buttons.DeleteButton {
        ClearButton(int x, int y, IPressable pressable) {
            super(x, y, 0, pressable);
            tooltip1.add(ClientUtil.xlate("modularrouters.guiText.tooltip.clearFilter"));
        }
    }

    static class MergeButton extends Buttons.AddButton {
        MergeButton(int x, int y, String locStr, String name, IPressable pressable) {
            super(x, y, pressable);
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "modularrouters.guiText.tooltip.mergeFilter", name, locStr);
        }
    }

    static class LoadButton extends TexturedButton {
        LoadButton(int x, int y, String locStr, String name,  IPressable pressable) {
            super( x, y, 16, 16, pressable);
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "modularrouters.guiText.tooltip.loadFilter", name, locStr);
        }

        @Override
        protected int getTextureX() {
            return 144;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }
}
