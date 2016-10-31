package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.gui.BackButton;
import me.desht.modularrouters.gui.RedstoneBehaviourButton;
import me.desht.modularrouters.gui.widgets.*;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import me.desht.modularrouters.item.module.Module.RelativeDirection;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiModule extends GuiContainerBase implements GuiPageButtonList.GuiResponder {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/module.png");
    private static final int REGULATOR_TEXTFIELD_ID = 0;
    static final int DIRECTION_BASE_ID = ModuleFlags.values().length;
    private static final int BACK_BUTTON_ID = DIRECTION_BASE_ID + RelativeDirection.values().length;
    private static final int REDSTONE_BUTTON_ID = BACK_BUTTON_ID + 1;
    private static final int REGULATOR_TOOLTIP_ID = BACK_BUTTON_ID + 1;

    /**
     * Base ID for extra buttons added by submodules
     */
    static final int EXTRA_BUTTON_BASE = 1000;
    /**
     * Base ID for extra textfields added by submodules
     */
    static final int EXTRA_TEXTFIELD_BASE = 1000;

    private static final int GUI_HEIGHT = 182;
    private static final int GUI_WIDTH = 192;
    static final int BUTTON_WIDTH = 16;
    static final int BUTTON_HEIGHT = 16;

    final ItemStack moduleItemStack;
    private final Module module;
    private final BlockPos routerPos;
    private final int moduleSlotIndex;
    private final EnumHand hand;
    private RelativeDirection facing;
    private int sendDelay;
    private int regulatorAmount;
    private RedstoneBehaviourButton rbb;
    private DirectionButton[] directionButtons = new DirectionButton[RelativeDirection.values().length];
    private ModuleToggleButton[] toggleButtons = new ModuleToggleButton[ModuleFlags.values().length];

    public GuiModule(ModuleContainer containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModule(ModuleContainer containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem);
        this.moduleItemStack = containerItem.filterHandler.getModuleItemStack();
        this.module = ItemModule.getModule(moduleItemStack);
        this.routerPos = routerPos;
        this.moduleSlotIndex = slotIndex;
        this.hand = hand;
        this.facing = ModuleHelper.getDirectionFromNBT(moduleItemStack);
        this.regulatorAmount = ModuleHelper.getRegulatorAmount(moduleItemStack);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        super.initGui();

        addToggleButton(ModuleFlags.BLACKLIST, 7, 75);
        addToggleButton(ModuleFlags.IGNORE_META, 24, 75);
        addToggleButton(ModuleFlags.IGNORE_NBT, 41, 75);
        addToggleButton(ModuleFlags.IGNORE_OREDICT, 58, 75);
        addToggleButton(ModuleFlags.TERMINATE, 75, 75);

        if (module.isDirectional()) {
            addDirectionButton(RelativeDirection.NONE, 70, 18);
            addDirectionButton(RelativeDirection.UP, 87, 18);
            addDirectionButton(RelativeDirection.LEFT, 70, 35);
            addDirectionButton(RelativeDirection.FRONT, 87, 35);
            addDirectionButton(RelativeDirection.RIGHT, 104, 35);
            addDirectionButton(RelativeDirection.DOWN, 87, 52);
            addDirectionButton(RelativeDirection.BACK, 104, 52);
        }

        if (ModuleHelper.isRedstoneBehaviourEnabled(moduleItemStack)) {
            rbb = new RedstoneBehaviourButton(REDSTONE_BUTTON_ID,
                    this.guiLeft + 92, this.guiTop + 75, BUTTON_WIDTH, BUTTON_HEIGHT, ModuleHelper.getRedstoneBehaviour(moduleItemStack));
            buttonList.add(rbb);
        }

        if (ModuleHelper.isRegulatorEnabled(moduleItemStack)) {
            TextFieldManager manager = createTextFieldManager();
            IntegerTextField field = new IntegerTextField(manager, REGULATOR_TEXTFIELD_ID, fontRendererObj, guiLeft + 166, guiTop + 77, 20, 12, 0, 64);
            field.setValue(ModuleHelper.getRegulatorAmount(moduleItemStack));
            field.setGuiResponder(this);
            buttonList.add(new RegulatorTooltipButton(REGULATOR_TOOLTIP_ID, guiLeft + 148, guiTop + 74));
        }

        if (routerPos != null) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, guiLeft - 12, guiTop));
        }
    }

    private void addToggleButton(ModuleFlags setting, int x, int y) {
        toggleButtons[setting.ordinal()] = new ModuleToggleButton(setting, this.guiLeft + x, this.guiTop + y);
        toggleButtons[setting.ordinal()].setToggled(ModuleHelper.checkFlag(moduleItemStack, setting));
        buttonList.add(toggleButtons[setting.ordinal()]);
    }

    private void addDirectionButton(RelativeDirection dir, int x, int y) {
        directionButtons[dir.ordinal()] = new DirectionButton(dir, this.guiLeft + x, this.guiTop + y);
        directionButtons[dir.ordinal()].setToggled(dir == facing);
        buttonList.add(directionButtons[dir.ordinal()]);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof DirectionButton) {
            for (RelativeDirection dir : RelativeDirection.values()) {
                DirectionButton db = getDirectionButton(dir);
                db.setToggled(db.id == button.id);
                if (db.isToggled()) {
                    facing = db.getDirection();
                }
            }
            sendModuleSettingsToServer();
        } else if (button instanceof ToggleButton) {
            ((ToggleButton) button).toggle();
            sendModuleSettingsToServer();
        } else if (button.id == BACK_BUTTON_ID) {
            if (routerPos != null) {
                ModularRouters.network.sendToServer(OpenGuiMessage.openRouter(routerPos));
            }
        } else if (button.id == REDSTONE_BUTTON_ID) {
            rbb.cycle(!isShiftKeyDown());
            sendModuleSettingsToServer();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (sendDelay > 0) {
            if (--sendDelay <= 0) {
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
        ModularRouters.network.sendToServer(new ModuleSettingsMessage(routerPos, moduleSlotIndex, hand, buildMessageData()));
    }

    /**
     * Encode the message data for this module.  This NBT data will be copied directly
     * into the module itemstack's NBT when the server receives the updateTextFields message.
     * Overriding subclasses must call the superclass method!
     *
     * @return the message data NBT
     */
    protected NBTTagCompound buildMessageData() {
        byte flags = (byte) (facing.ordinal() << 4);
        for (ModuleFlags setting : ModuleFlags.values()) {
            if (getToggleButton(setting).isToggled()) {
                flags |= setting.getMask();
            }
        }
        RouterRedstoneBehaviour behaviour = rbb == null ? RouterRedstoneBehaviour.ALWAYS : rbb.getState();
        NBTTagCompound compound = new NBTTagCompound();
        compound.setByte(ModuleHelper.NBT_FLAGS, flags);
        compound.setByte(ModuleHelper.NBT_REDSTONE_MODE, (byte) behaviour.ordinal());
        compound.setInteger(ModuleHelper.NBT_REGULATOR_AMOUNT, regulatorAmount);
        return compound;
    }

    private ModuleToggleButton getToggleButton(ModuleFlags flags) {
        // risk of class cast exception here, but should never happen unless something's gone horribly wrong
        //  - best to throw exception ASAP in that case
        return toggleButtons[flags.ordinal()];
    }

    private DirectionButton getDirectionButton(RelativeDirection direction) {
        // see above re: class cast exception
        return directionButtons[direction.ordinal()];
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
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if ((keyCode == Keyboard.KEY_ESCAPE || (keyCode == Keyboard.KEY_E && !isFocused())) && routerPos != null) {
            // Intercept ESC/E and immediately reopen the router GUI - this avoids an
            // annoying screen flicker between closing the module GUI and reopen the router GUI.
            // Sending the reopen message will also close this gui, triggering onGuiClosed()
            ModularRouters.network.sendToServer(OpenGuiMessage.openRouter(routerPos));
        } else if (typedChar == Config.configKey) {
            // trying to configure an installed smart filter?
            handleFilterConfig();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void handleFilterConfig() {
        Slot slot = getSlotUnderMouse();
        if (slot == null || !slot.getHasStack()) {
            return;
        }
        int filterSlotIndex = slot.slotNumber;
        if (filterSlotIndex >= 0 && filterSlotIndex < 9) {
            SmartFilter filter = ItemSmartFilter.getFilter(slot.getStack());
            if (filter == null) {
                return;
            }
            if (routerPos != null) {
                // module is installed in a router
                TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(mc.theWorld, routerPos);
                if (router != null) {
                    router.playerConfiguringModule(mc.thePlayer, moduleSlotIndex, slot.getSlotIndex());
                    if (filter.hasGuiContainer()) {
                        ModularRouters.network.sendToServer(OpenGuiMessage.openFilterInInstalledModule(routerPos, moduleSlotIndex, filterSlotIndex));
                    } else {
                        // no container, just open the client-side GUI directly
                        mc.thePlayer.openGui(ModularRouters.instance, ModularRouters.GUI_FILTER_INSTALLED, mc.theWorld,
                                routerPos.getX(), routerPos.getY(), routerPos.getZ());
                    }
                }
            } else if (hand != null) {
                // module is in player's hand
                if (filter.hasGuiContainer()) {
                    ModularRouters.network.sendToServer(OpenGuiMessage.openFilterInModule(filterSlotIndex));
                } else {
                    // no container, just open the client-side GUI directly
                    // record the filter slot in the module itemstack's NBT - we'll need this when opening the GUI later
                    ItemModule.setFilterConfigSlot(mc.thePlayer.getHeldItem(hand), filterSlotIndex);
                    mc.thePlayer.openGui(ModularRouters.instance,
                            hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_FILTER_HELD_MAIN : ModularRouters.GUI_FILTER_HELD_OFF,
                            mc.theWorld, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (sendDelay > 0) {
            // ensure no delayed updates get lost
            sendModuleSettingsToServer();
        }
    }

    @Override
    public void setEntryValue(int id, boolean value) {
        // nothing
    }

    @Override
    public void setEntryValue(int id, float value) {
        // nothing
    }

    @Override
    public void setEntryValue(int id, String value) {
        if (id == REGULATOR_TEXTFIELD_ID) {
            regulatorAmount = value.isEmpty() ? 0 : Integer.parseInt(value);
            sendModuleSettingsDelayed(5);
        }
    }

    private static class RegulatorTooltipButton extends TexturedButton {
        public RegulatorTooltipButton(int buttonId, int x, int y) {
            super(buttonId, x, y, 16, 16);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.regulatorTooltip");
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.intFieldTooltip");
        }

        @Override
        protected int getTextureX() {
            return 112;
        }

        @Override
        protected int getTextureY() {
            return 0;
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }
}
