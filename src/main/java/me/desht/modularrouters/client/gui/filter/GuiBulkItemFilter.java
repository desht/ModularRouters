package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.BackButton;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class GuiBulkItemFilter extends GuiFilterContainer {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/bulkitemfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 233;

    private ModuleTarget target;

    public GuiBulkItemFilter(ContainerSmartFilter container, PlayerInventory inventory, ITextComponent displayName) {
        super(container, inventory, displayName);

        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        addButton(new Buttons.ClearButton(guiLeft + 8, guiTop + 130,
                p -> PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.CLEAR_ALL, container.getLocator(), null))
        ));

        MFLocator locator = container.getLocator();
        if (locator.filterSlot >= 0) {
            // in a module; add a back button to go back to module gui
            addButton(new BackButton(guiLeft - 12, guiTop, p -> closeGUI()));
        }

        if (locator.routerSlot >= 0 && locator.routerPos != null) {
            // in a module in a router; add buttons to merge/load the module's target inventory
            ItemStack moduleStack = locator.getTargetItem(Minecraft.getInstance().player);
            TileEntityItemRouter router = container.getRouter();
            CompiledModule cm = ((ItemModule) moduleStack.getItem()).compile(router, moduleStack);
            target = cm.getActualTarget(router);
            // This should work even if the target is in another dimension, since the target name
            // is stored in the module item NBT, which was set up server-side.
            // Using getActualTarget() here *should* ensure that we always see the right target...
            if (target != null && target.invName != null && !target.invName.isEmpty()) {
                addButton(new Buttons.MergeButton(guiLeft + 28, guiTop + 130,
                        MiscUtil.locToString(target.dimId, target.pos), target.invName, p -> {
                    if (target != null) {
                        PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                                Operation.MERGE, container.getLocator(), target.toNBT()));
                    }
                }));
                addButton(new Buttons.LoadButton(guiLeft + 48, guiTop + 130,
                        MiscUtil.locToString(target.dimId, target.pos), target.invName, p -> {
                    if (target != null) {
                        PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                                Operation.LOAD, container.getLocator(), target.toNBT()));
                    }
                }));
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(title, this.xSize / 2f - font.getStringWidth(title) / 2f, 8, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(textureLocation);
        blit(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
