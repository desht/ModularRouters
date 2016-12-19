package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.BackButton;
import me.desht.modularrouters.gui.RedstoneBehaviourButton;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.gui.widgets.button.RadioButton;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.gui.widgets.button.ToggleButton;
import me.desht.modularrouters.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import me.desht.modularrouters.item.module.Module.RelativeDirection;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.Range;
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
    protected final boolean regulationEnabled;
    private RelativeDirection facing;
    private int sendDelay;
    private int regulatorAmount;
    private RedstoneBehaviourButton rbb;
    private DirectionButton[] directionButtons = new DirectionButton[RelativeDirection.values().length];
    private ModuleToggleButton[] toggleButtons = new ModuleToggleButton[ModuleFlags.values().length];

    public GuiModule(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModule(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem);
        this.moduleItemStack = containerItem.filterHandler.getHoldingItemStack();
        this.module = ItemModule.getModule(moduleItemStack);
        this.routerPos = routerPos;
        this.moduleSlotIndex = slotIndex;
        this.hand = hand;
        this.facing = ModuleHelper.getDirectionFromNBT(moduleItemStack);
        this.regulationEnabled = ModuleHelper.isRegulatorEnabled(moduleItemStack);
        this.regulatorAmount = ModuleHelper.getRegulatorAmount(moduleItemStack);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        super.initGui();

        addToggleButton(ModuleFlags.BLACKLIST, 7, 75);
        addToggleButton(ModuleFlags.IGNORE_META, 25, 75);
        addToggleButton(ModuleFlags.IGNORE_NBT, 43, 75);
        addToggleButton(ModuleFlags.IGNORE_OREDICT, 61, 75);
        addToggleButton(ModuleFlags.TERMINATE, 79, 75);

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
                    this.guiLeft + 97, this.guiTop + 75, BUTTON_WIDTH, BUTTON_HEIGHT, ModuleHelper.getRedstoneBehaviour(moduleItemStack));
            buttonList.add(rbb);
        }

        if (regulationEnabled) {
            TextFieldManager manager = createTextFieldManager();
            Range<Integer> range = module.isFluidModule() ? Range.between(0, 100) : Range.between(0, 64);
            int xOff = module.isFluidModule() ? 0 : 10;
            IntegerTextField field = new IntegerTextField(manager, REGULATOR_TEXTFIELD_ID, fontRendererObj, guiLeft + 156 + xOff, guiTop + 77,
                    20, 12, range.getMinimum(), range.getMaximum());
            field.setValue(ModuleHelper.getRegulatorAmount(moduleItemStack));
            field.setGuiResponder(this);
            buttonList.add(new RegulatorTooltipButton(REGULATOR_TOOLTIP_ID, guiLeft + 138 + xOff, guiTop + 74, module.isFluidModule()));
        }

        if (routerPos != null) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, guiLeft - 12, guiTop));
        }
    }

    private void addToggleButton(ModuleFlags flag, int x, int y) {
        toggleButtons[flag.ordinal()] = new ModuleToggleButton(flag, this.guiLeft + x, this.guiTop + y);
        toggleButtons[flag.ordinal()].setToggled(ModuleHelper.checkFlag(moduleItemStack, flag));
        buttonList.add(toggleButtons[flag.ordinal()]);
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
     * into the module itemstack's NBT when the server receives the module settings message.
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
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        if (!module.isDirectional()) {
            drawTexturedModalRect(guiLeft + 69, guiTop + 17, 204, 0, 52, 52);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if ((keyCode == Keyboard.KEY_ESCAPE || (keyCode == Keyboard.KEY_E && !isFocused())) && routerPos != null) {
            // Intercept ESC/E and immediately reopen the router GUI - this avoids an
            // annoying screen flicker between closing the module GUI and reopen the router GUI.
            // Sending the reopen message will also close this gui, triggering onGuiClosed()
            ModularRouters.network.sendToServer(OpenGuiMessage.openRouter(routerPos));
            return;
        } else if (typedChar == Config.configKey && handleFilterConfig()) {
            // trying to configure an installed smart filter, we're done
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        if (btn != 2 || !handleFilterConfig()) {
            super.mouseClicked(x, y, btn);
        }
    }

    private boolean handleFilterConfig() {
        Slot slot = getSlotUnderMouse();
        if (slot == null || ItemSmartFilter.getFilter(slot.getStack()) == null || slot.slotNumber < 0 || slot.slotNumber >= Filter.FILTER_SIZE) {
            return false;
        }
        int filterSlotIndex = slot.slotNumber;
        SmartFilter filter = ItemSmartFilter.getFilter(slot.getStack());
        TileEntityItemRouter router = getItemRouterTE();
        if (router != null) {
            // module is installed in a router
            router.playerConfiguringModule(mc.player, moduleSlotIndex, slot.getSlotIndex());
            if (filter.hasGuiContainer()) {
                ModularRouters.network.sendToServer(OpenGuiMessage.openFilterInInstalledModule(routerPos, moduleSlotIndex, filterSlotIndex));
            } else {
                // no container, just open the client-side GUI directly
                mc.player.openGui(ModularRouters.instance, ModularRouters.GUI_FILTER_INSTALLED, mc.world,
                        routerPos.getX(), routerPos.getY(), routerPos.getZ());
            }
        } else if (hand != null) {
            // module is in player's hand
            // record the filter slot in the module itemstack's NBT - we'll need this when opening the GUI later
            ItemModule.setFilterConfigSlot(mc.player.getHeldItem(hand), filterSlotIndex);
            if (filter.hasGuiContainer()) {
                ModularRouters.network.sendToServer(OpenGuiMessage.openFilterInModule(hand, filterSlotIndex));
            } else {
                // no container, just open the client-side GUI directly
                mc.player.openGui(ModularRouters.instance,
                        hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_FILTER_HELD_MAIN : ModularRouters.GUI_FILTER_HELD_OFF,
                        mc.world, 0, 0, 0);
            }
        }
        return true;
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

    protected TileEntityItemRouter getItemRouterTE() {
        if (routerPos != null) {
            TileEntity te = Minecraft.getMinecraft().world.getTileEntity(routerPos);
            return te instanceof TileEntityItemRouter ? (TileEntityItemRouter) te : null;
        }
        return null;
    }

    private static class RegulatorTooltipButton extends TexturedButton {
        public RegulatorTooltipButton(int buttonId, int x, int y, boolean isFluid) {
            super(buttonId, x, y, 16, 16);
            MiscUtil.appendMultiline(tooltip1, isFluid ? "guiText.tooltip.fluidRegulatorTooltip" : "guiText.tooltip.regulatorTooltip");
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.numberFieldTooltip");
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

    private static class ModuleToggleButton extends TexturedToggleButton {
        ModuleToggleButton(ModuleFlags setting, int x, int y) {
            super(setting.ordinal(), x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip." + ModuleFlags.values()[id] + ".1");
            MiscUtil.appendMultiline(tooltip2, "guiText.tooltip." + ModuleFlags.values()[id] + ".2");
        }

        @Override
        protected int getTextureX() {
            return this.id * BUTTON_WIDTH * 2 + (isToggled() ? BUTTON_WIDTH : 0);
        }

        @Override
        protected int getTextureY() {
            return 32;
        }
    }

    private static class DirectionButton extends RadioButton {
        private static final int DIRECTION_GROUP = 1;
        private final RelativeDirection direction;

        public DirectionButton(RelativeDirection dir, int x, int y) {
            super(dir.ordinal() + DIRECTION_BASE_ID, DIRECTION_GROUP, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            this.direction = dir;
            tooltip1.add(TextFormatting.GRAY + I18n.format("guiText.tooltip." + dir));
            tooltip2.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip." + dir));
        }

        @Override
        protected int getTextureX() {
            return direction.ordinal() * BUTTON_WIDTH * 2 + (isToggled() ? BUTTON_WIDTH : 0);
        }

        @Override
        protected int getTextureY() {
            return 48;
        }

        public RelativeDirection getDirection() {
            return direction;
        }
    }
}
