package me.desht.modularrouters.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.gui.BackButton;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiBulkItemFilter extends GuiFilterContainer {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/bulkitemfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 233;

    private static final int CLEAR_BUTTON_ID = 1;
    private static final int MERGE_BUTTON_ID = 2;
    private static final int LOAD_BUTTON_ID = 3;
    private static final int BACK_BUTTON_ID = 100;

    private ModuleTarget target;

    public GuiBulkItemFilter(ContainerSmartFilter container, BlockPos routerPos, Integer moduleSlotIndex, Integer filterSlotIndex, EnumHand hand) {
        super(container, routerPos, moduleSlotIndex, filterSlotIndex, hand);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();

        buttonList.add(new Buttons.ClearButton(CLEAR_BUTTON_ID, guiLeft + 8, guiTop + 130));

        if (filterSlotIndex >= 0) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, guiLeft - 12, guiTop));
        }
        if (moduleSlotIndex >= 0) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(mc.world, routerPos);
            if (router != null) {
                ItemStack moduleStack = router.getModules().getStackInSlot(moduleSlotIndex);
                Module m = ItemModule.getModule(moduleStack);
                CompiledModule cm = m.compile(router, moduleStack);
                target = cm.getActualTarget(router);
                // This should work even if the target is in another dimension, since the target name
                // is stored in the module item NBT, which was set up server-side.
                // Using getActualTarget() here *should* ensure that we always see the right target...
                if (target != null && target.invName != null && !target.invName.isEmpty()) {
                    buttonList.add(new Buttons.MergeButton(MERGE_BUTTON_ID, guiLeft + 28, guiTop + 130,
                            MiscUtil.locToString(target.dimId, target.pos), target.invName));
                    buttonList.add(new Buttons.LoadButton(LOAD_BUTTON_ID, guiLeft + 48, guiTop + 130,
                            MiscUtil.locToString(target.dimId, target.pos), target.invName));
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(title, this.xSize / 2 - fontRenderer.getStringWidth(title) / 2, 8, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case CLEAR_BUTTON_ID:
                if (routerPos != null) {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.CLEAR_ALL, routerPos, moduleSlotIndex, filterSlotIndex, null));
                } else {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.CLEAR_ALL, hand, filterSlotIndex, null));
                }
                break;
            case MERGE_BUTTON_ID:
                if (target != null) {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.MERGE, routerPos, moduleSlotIndex, filterSlotIndex, target.toNBT()));
                }
                break;
            case LOAD_BUTTON_ID:
                if (target != null) {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.LOAD, routerPos, moduleSlotIndex, filterSlotIndex, target.toNBT()));
                }
                break;
            case BACK_BUTTON_ID:
                closeGUI();
                break;
            default:
                super.actionPerformed(button);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
