package me.desht.modularrouters.gui.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.FilterContainer;
import me.desht.modularrouters.container.ModFilterContainer;
import me.desht.modularrouters.gui.BackButton;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class GuiModFilter extends GuiContainerBase {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/modfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 252;

    private static final int ADD_BUTTON_ID = 1;
    private static final int BACK_BUTTON_ID = 2;
    private static final int BASE_REMOVE_ID = 100;

    private final BlockPos routerPos;
    private final int moduleSlotIndex;
    private final int filterSlotIndex;
    private final EnumHand hand;
    private final ItemStack filterStack;
    private final List<String> mods = Lists.newArrayList();

    private ItemStack prevInSlot = null;
    private String modId = "";
    private String modName = "";

    public GuiModFilter(FilterContainer container, BlockPos routerPos, Integer moduleSlotIndex, Integer filterSlotIndex, EnumHand hand) {
        super(container);

        this.routerPos = routerPos;
        this.moduleSlotIndex = moduleSlotIndex;
        this.filterSlotIndex = filterSlotIndex;
        this.hand = hand;

        this.filterStack = ((ModFilterContainer) container).filterStack;

        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;

        mods.addAll(ModFilter.getModList(filterStack));
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();
        if (filterSlotIndex >= 0) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, guiLeft - 12, guiTop));
        }
        buttonList.add(new Buttons.AddButton(ADD_BUTTON_ID, guiLeft + 154, guiTop + 19));
        for (int i = 0; i < mods.size(); i++) {
            buttonList.add(new Buttons.DeleteButton(BASE_REMOVE_ID + i, guiLeft + 8, guiTop + 44 + i * 19));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = filterStack.getDisplayName() + (routerPos != null ? I18n.format("guiText.label.installed") : "");
        fontRendererObj.drawString(title, this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2, 8, 0x404040);

        if (!modName.isEmpty()) {
            fontRendererObj.drawString(modName, 29, 23, 0x404040);
        }

        for (int i = 0; i < mods.size(); i++) {
            String mod = ModNameCache.getModName(mods.get(i));
            fontRendererObj.drawString(mod, 28, 47 + i * 19, 0x404080);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        ItemStack inSlot = inventorySlots.getInventory().get(0);
        if (inSlot == null && prevInSlot != null) {
            modId = modName = "";
        } else if (inSlot != null && (prevInSlot == null || !inSlot.isItemEqualIgnoreDurability(prevInSlot))) {
            modId = inSlot.getItem().getRegistryName().getResourceDomain();
            modName = ModNameCache.getModName(modId);
        }
        prevInSlot = inSlot;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ADD_BUTTON_ID && !modId.isEmpty()) {
            NBTTagCompound ext = new NBTTagCompound();
            ext.setString("ModId", modId);
            if (routerPos != null) {
                ModularRouters.network.sendToServer(new FilterSettingsMessage(
                        FilterSettingsMessage.Operation.ADD_STRING, routerPos, moduleSlotIndex, filterSlotIndex, ext));
            } else {
                ModularRouters.network.sendToServer(new FilterSettingsMessage(
                        FilterSettingsMessage.Operation.ADD_STRING, hand, filterSlotIndex, ext));
            }
            inventorySlots.inventorySlots.get(0).putStack(null);
        } else if (button.id >= BASE_REMOVE_ID && button.id < BASE_REMOVE_ID + mods.size()) {
            NBTTagCompound ext = new NBTTagCompound();
            ext.setInteger("Pos", button.id - BASE_REMOVE_ID);
            if (routerPos != null) {
                ModularRouters.network.sendToServer(new FilterSettingsMessage(
                        FilterSettingsMessage.Operation.REMOVE_AT, routerPos, moduleSlotIndex, filterSlotIndex, ext));
            } else {
                ModularRouters.network.sendToServer(new FilterSettingsMessage(
                        FilterSettingsMessage.Operation.REMOVE_AT, hand, filterSlotIndex, ext));
            }
        } else if (button.id == BACK_BUTTON_ID) {
            closeGUI();
        } else {
            super.actionPerformed(button);
        }
    }

    private boolean closeGUI() {
        if (routerPos != null) {
            // need to re-open module GUI for module in router slot <moduleSlotIndex>
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(mc.theWorld, routerPos);
            if (router != null) {
                router.playerConfiguringModule(mc.thePlayer, moduleSlotIndex);
                ModularRouters.network.sendToServer(OpenGuiMessage.openModuleInRouter(routerPos, moduleSlotIndex));
                return true;
            }
        } else if (hand != null) {
            ItemStack stack = mc.thePlayer.getHeldItem(hand);
            if (ItemModule.getModule(stack) != null) {
                // need to re-open module GUI for module in player's hand
                ModularRouters.network.sendToServer(OpenGuiMessage.openModuleInHand(hand));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if ((keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_E)) {
            // Intercept ESC/E and immediately reopen the previous GUI, if any
            if (closeGUI()) return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void resync(ItemStack filterStack) {
        mods.clear();
        mods.addAll(ModFilter.getModList(filterStack));
        initGui();
    }
}
