package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.BackButton;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiBulkItemFilter extends GuiFilterContainer {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/bulkitemfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 233;

    private static final int CLEAR_BUTTON_ID = 1;
    private static final int MERGE_BUTTON_ID = 2;
    private static final int LOAD_BUTTON_ID = 3;
    private static final int BACK_BUTTON_ID = 100;

    private ModuleTarget target;

    public GuiBulkItemFilter(ContainerSmartFilter container) {
        super(container);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

//        buttonList.clear();

        addButton(new Buttons.ClearButton(CLEAR_BUTTON_ID, guiLeft + 8, guiTop + 130) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                if (router != null) {
                    PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.CLEAR_ALL, router.getPos(), null));
                } else {
                    PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.CLEAR_ALL, hand, null));
                }
            }
        });

        SlotTracker tracker = SlotTracker.getInstance(mc.player);
        if (tracker.getFilterSlot() >= 0) {
            addButton(new BackButton(BACK_BUTTON_ID, guiLeft - 12, guiTop) {
                @Override
                public void onClick(double p_194829_1_, double p_194829_3_) {
                    closeGUI();
                }
            });
        }
        if (tracker.getModuleSlot() >= 0 && router != null) {
            ItemStack moduleStack = tracker.getConfiguringModule(router);
//                ItemStack moduleStack = router.getModules().getStackInSlot(moduleSlotIndex);
//                ItemModule m = (ItemModule) moduleStack.getItem();
//                Module m = ItemModule.getModule(moduleStack);
            CompiledModule cm = ((ItemModule) moduleStack.getItem()).compile(router, moduleStack);
            target = cm.getActualTarget(router);
            // This should work even if the target is in another dimension, since the target name
            // is stored in the module item NBT, which was set up server-side.
            // Using getActualTarget() here *should* ensure that we always see the right target...
            if (target != null && target.invName != null && !target.invName.isEmpty()) {
                addButton(new Buttons.MergeButton(MERGE_BUTTON_ID, guiLeft + 28, guiTop + 130,
                        MiscUtil.locToString(target.dimId, target.pos), target.invName) {
                    @Override
                    public void onClick(double p_194829_1_, double p_194829_3_) {
                        if (target != null) {
                            PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                                    FilterSettingsMessage.Operation.MERGE, router.getPos(), target.toNBT()));
                        }
                    }
                });
                addButton(new Buttons.LoadButton(LOAD_BUTTON_ID, guiLeft + 48, guiTop + 130,
                        MiscUtil.locToString(target.dimId, target.pos), target.invName) {
                    @Override
                    public void onClick(double p_194829_1_, double p_194829_3_) {
                        if (target != null) {
                            PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                                    FilterSettingsMessage.Operation.LOAD, router.getPos(), target.toNBT()));
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(title, this.xSize / 2f - fontRenderer.getStringWidth(title) / 2f, 8, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
