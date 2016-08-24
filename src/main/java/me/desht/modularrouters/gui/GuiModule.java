package me.desht.modularrouters.gui;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import me.desht.modularrouters.item.module.Module.RelativeDirection;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.network.ReopenRouterMessage;
import me.desht.modularrouters.proxy.CommonProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class GuiModule extends GuiContainerBase {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/module.png");
    static final int DIRECTION_BASE_ID = ModuleFlags.values().length;
    private static final int GUI_HEIGHT = 181;
    private static final int GUI_WIDTH = 192;
    static final int BUTTON_WIDTH = 16;
    static final int BUTTON_HEIGHT = 16;

    protected final ItemStack moduleItemStack;
    private final BlockPos routerPos;
    private final int slotIndex;
    private RelativeDirection facing;
    private final EnumHand hand;
    private int sendDelay;

    public GuiModule(ModuleContainer containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModule(ModuleContainer containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem);
        moduleItemStack = containerItem.filterHandler.getModuleItemStack();
        this.routerPos = routerPos;
        this.slotIndex = slotIndex;
        this.hand = hand;
        facing = Module.getDirectionFromNBT(moduleItemStack);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        addToggleButton(ModuleFlags.BLACKLIST, 7, 74);
        addToggleButton(ModuleFlags.IGNORE_META, 24, 74);
        addToggleButton(ModuleFlags.IGNORE_NBT, 41, 74);
        addToggleButton(ModuleFlags.IGNORE_OREDICT, 58, 74);
        addToggleButton(ModuleFlags.TERMINATE, 75, 74);

        addDirectionButton(RelativeDirection.NONE, 70, 18);
        addDirectionButton(RelativeDirection.UP, 87, 18);
        addDirectionButton(RelativeDirection.LEFT, 70, 35);
        addDirectionButton(RelativeDirection.FRONT, 87, 35);
        addDirectionButton(RelativeDirection.RIGHT, 104, 35);
        addDirectionButton(RelativeDirection.DOWN, 87, 52);
        addDirectionButton(RelativeDirection.BACK, 104, 52);
    }

    private void addToggleButton(ModuleFlags setting, int x, int y) {
        ModuleToggleButton tb = new ModuleToggleButton(setting, this.guiLeft + x, this.guiTop + y);
        tb.setToggled(Module.checkFlag(moduleItemStack, setting));
        this.buttonList.add(tb);
    }

    private void addDirectionButton(RelativeDirection dir, int x, int y) {
        DirectionButton db = new DirectionButton(dir, this.guiLeft + x, this.guiTop + y);
        db.setToggled(dir == facing);
        this.buttonList.add(db);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof ModuleToggleButton) {
            ((ModuleToggleButton) button).toggle();
        } else if (button instanceof DirectionButton) {
            for (RelativeDirection dir : RelativeDirection.values()) {
                DirectionButton db = getDirectionButton(dir);
                db.setToggled(db.id == button.id);
                if (db.isToggled()) {
                    facing = db.getDirection();
                }
            }
        }

        sendModuleSettingsToServer();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (sendDelay > 0) {
            sendDelay--;
            if (sendDelay <= 0) {
                sendModuleSettingsToServer();
            }
        }
    }

    /**
     * Delaying sending of module settings reduces the number of partial updates from e.g. textfields being edited.
     *
     * @param delay delay in ticks
     */
    void sendModuleSettingsDelayed(int delay) {
        sendDelay = delay;
    }

    void sendModuleSettingsToServer() {
        byte flags = (byte) (facing.ordinal() << 4);
        for (ModuleFlags setting : ModuleFlags.values()) {
            if (getToggleButton(setting).isToggled()) {
                flags |= setting.getMask();
            }
        }

        CommonProxy.network.sendToServer(new ModuleSettingsMessage(flags, routerPos, slotIndex, hand, getExtMessageData()));
    }

    protected NBTTagCompound getExtMessageData() {
        return null;
    }

    private ModuleToggleButton getToggleButton(ModuleFlags flags) {
        // risk of class cast exception here, but should never happen unless something's gone horribly wrong
        //  - best to throw exception ASAP in that case
        return (ModuleToggleButton) buttonList.get(flags.ordinal());
    }

    private DirectionButton getDirectionButton(RelativeDirection direction) {
        // see above re: class cast exception
        return (DirectionButton) buttonList.get(direction.ordinal() + DIRECTION_BASE_ID);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        String title = moduleItemStack.getDisplayName() + (routerPos != null ? I18n.format("guiText.label.installed") : "");
        this.fontRendererObj.drawString(title, this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2, 5, 0x404040);
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
    public void onGuiClosed() {
        super.onGuiClosed();
        if (sendDelay > 0) {
            sendModuleSettingsToServer();
        }
        if (routerPos != null) {
            // re-open router GUI; we were editing an installed module
            CommonProxy.network.sendToServer(new ReopenRouterMessage(routerPos));
        }
    }

}
