package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.BackButton;
import me.desht.modularrouters.client.gui.IMouseOverHelpProvider;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.RedstoneBehaviourButton;
import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.client.gui.widgets.button.RadioButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.gui.widgets.button.ToggleButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.item.augment.Augment;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.augment.ItemAugment.AugmentType;
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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.Range;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiModule extends GuiContainerBase implements GuiPageButtonList.GuiResponder, IContainerListener, IMouseOverHelpProvider {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/module.png");
    private static final int REGULATOR_TEXTFIELD_ID = 0;
    static final int DIRECTION_BASE_ID = ModuleFlags.values().length;
    private static final int BACK_BUTTON_ID = DIRECTION_BASE_ID + RelativeDirection.values().length;
    private static final int REDSTONE_BUTTON_ID = BACK_BUTTON_ID + 1;
    private static final int REGULATOR_TOOLTIP_ID = BACK_BUTTON_ID + 2;
    private static final int MOUSEOVER_BUTTON_ID = BACK_BUTTON_ID + 3;

    // locations of extra textures on the gui module texture sheet
    static final Point SMALL_TEXTFIELD_XY = new Point(0, 198);
    static final Point LARGE_TEXTFIELD_XY = new Point(0, 212);
    static final Point BUTTON_XY = new Point(0, 226);

    // Base ID for extra buttons added by module subclasses
    static final int EXTRA_BUTTON_BASE = 1000;
    // Base ID for extra textfields added by module subclasses
    static final int EXTRA_TEXTFIELD_BASE = 1000;

    private static final int GUI_HEIGHT = 198;
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
    protected int regulatorAmount;
    private RedstoneBehaviourButton redstoneButton;
    protected IntegerTextField regulatorTextField;
    private RegulatorTooltipButton regulatorTooltipButton;
    private DirectionButton[] directionButtons = new DirectionButton[RelativeDirection.values().length];
    private ModuleToggleButton[] toggleButtons = new ModuleToggleButton[ModuleFlags.values().length];
    private final MouseOverHelp mouseOverHelp;
    protected ItemAugment.AugmentCounter augmentCounter;
    private MouseOverHelp.Button mouseOverHelpButton;

    public GuiModule(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModule(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem);
        this.mouseOverHelp = new MouseOverHelp(this);
        this.moduleItemStack = containerItem.filterHandler.getHolderStack();
        this.module = ItemModule.getModule(moduleItemStack);
        this.routerPos = routerPos;
        this.moduleSlotIndex = slotIndex;
        this.hand = hand;
        this.facing = ModuleHelper.getDirectionFromNBT(moduleItemStack);
        this.regulatorAmount = ModuleHelper.getRegulatorAmount(moduleItemStack);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        this.augmentCounter = new ItemAugment.AugmentCounter(moduleItemStack);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        super.initGui();

        addToggleButton(ModuleFlags.BLACKLIST, 7, 75);
        addToggleButton(ModuleFlags.IGNORE_META, 7, 93);
        addToggleButton(ModuleFlags.IGNORE_NBT, 25, 75);
        addToggleButton(ModuleFlags.IGNORE_OREDICT, 25, 93);
        addToggleButton(ModuleFlags.TERMINATE, 45, 93);

        if (module.isDirectional()) {
            addDirectionButton(RelativeDirection.NONE, 70, 18);
            addDirectionButton(RelativeDirection.UP, 87, 18);
            addDirectionButton(RelativeDirection.LEFT, 70, 35);
            addDirectionButton(RelativeDirection.FRONT, 87, 35);
            addDirectionButton(RelativeDirection.RIGHT, 104, 35);
            addDirectionButton(RelativeDirection.DOWN, 87, 52);
            addDirectionButton(RelativeDirection.BACK, 104, 52);
        }

        mouseOverHelpButton = new MouseOverHelp.Button(MOUSEOVER_BUTTON_ID, guiLeft + 175, guiTop + 1, mouseOverHelp);
        buttonList.add(mouseOverHelpButton);

        redstoneButton = new RedstoneBehaviourButton(REDSTONE_BUTTON_ID,
                this.guiLeft + 170, this.guiTop + 93, BUTTON_WIDTH, BUTTON_HEIGHT, ModuleHelper.getRedstoneBehaviour(moduleItemStack));
        buttonList.add(redstoneButton);

        TextFieldManager manager = createTextFieldManager();
        Range<Integer> range = module.isFluidModule() ? Range.between(0, 100) : Range.between(0, 64);
        int xOff = module.isFluidModule() ? 0 : 10;
        regulatorTextField = new IntegerTextField(manager, REGULATOR_TEXTFIELD_ID, fontRenderer, guiLeft + 156 + xOff, guiTop + 75,
                20, 12, range.getMinimum(), range.getMaximum());
        regulatorTextField.setValue(regulatorAmount);
        regulatorTextField.setGuiResponder(this);
        regulatorTooltipButton = new RegulatorTooltipButton(REGULATOR_TOOLTIP_ID, guiLeft + 138 + xOff, guiTop + 73, module.isFluidModule());
        buttonList.add(regulatorTooltipButton);

        if (routerPos != null) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, guiLeft + 2, guiTop + 1));
        }

        mouseOverHelp.addHelpRegion(guiLeft + 7, guiTop + 16, guiLeft + 60, guiTop + 69, "guiText.popup.filter");
        mouseOverHelp.addHelpRegion(guiLeft + 5, guiTop + 73, guiLeft + 62, guiTop + 110, "guiText.popup.filterControl");
        mouseOverHelp.addHelpRegion(guiLeft + 68, guiTop + 16, guiLeft + 121, guiTop + 69, module.isDirectional() ? "guiText.popup.direction" : "guiText.popup.noDirection");
        mouseOverHelp.addHelpRegion(guiLeft + 77, guiTop + 74, guiLeft + 112, guiTop + 109, "guiText.popup.augments");
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        inventorySlots.removeListener(this);
        inventorySlots.addListener(this);
        setupButtonVisibility();
    }

    protected void setupButtonVisibility() {
        redstoneButton.visible = augmentCounter.getAugmentCount(AugmentType.REDSTONE) > 0;
        regulatorTooltipButton.visible = augmentCounter.getAugmentCount(AugmentType.REGULATOR) > 0;
        regulatorTextField.setVisible(augmentCounter.getAugmentCount(AugmentType.REGULATOR) > 0);
    }

    private void addToggleButton(ModuleFlags flag, int x, int y) {
        toggleButtons[flag.ordinal()] = new ModuleToggleButton(flag, this.guiLeft + x, this.guiTop + y, ModuleHelper.checkFlag(moduleItemStack, flag));
        buttonList.add(toggleButtons[flag.ordinal()]);
    }

    private void addDirectionButton(RelativeDirection dir, int x, int y) {
        directionButtons[dir.ordinal()] = new DirectionButton(dir, module, this.guiLeft + x, this.guiTop + y, dir == facing);
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
            if (((ToggleButton) button).sendToServer())
                sendModuleSettingsToServer();
        } else if (button.id == BACK_BUTTON_ID) {
            if (routerPos != null) {
                ModularRouters.network.sendToServer(OpenGuiMessage.openRouter(routerPos));
            }
        } else if (button.id == REDSTONE_BUTTON_ID) {
            redstoneButton.cycle(!isShiftKeyDown());
            sendModuleSettingsToServer();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        mouseOverHelp.setActive(mouseOverHelpButton.isToggled());
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
        RouterRedstoneBehaviour behaviour = redstoneButton == null ? RouterRedstoneBehaviour.ALWAYS : redstoneButton.getState();
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
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 5, getFgColor(module.getItemTint()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Color c = getGuiBackgroundTint();
        GL11.glColor4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        if (!module.isDirectional()) {
            drawTexturedModalRect(guiLeft + 69, guiTop + 17, 204, 0, 52, 52);
        }
    }

    private Color getGuiBackgroundTint() {
        if (ConfigHandler.module.backgroundTint) {
            Color c = module.getItemTint();
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            return Color.getHSBColor(hsb[0], hsb[1] * 0.7f, hsb[2]);
        } else {
            return Color.WHITE;
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
        } else if (typedChar == ConfigHandler.getConfigKey() && handleFilterConfig()) {
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
            ModuleHelper.setFilterConfigSlot(mc.player.getHeldItem(hand), filterSlotIndex);
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

    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
    }

    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if (slotInd >= ContainerModule.AUGMENT_START && slotInd < ContainerModule.AUGMENT_START + Augment.SLOTS) {
            augmentCounter = new ItemAugment.AugmentCounter(moduleItemStack);
            setupButtonVisibility();
        }
    }

    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
    }

    @Override
    public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
    }

    private static final int THRESHOLD = 129;
    private int getFgColor(Color c) {
        int luminance = (int) Math.sqrt(c.getRed() * c.getRed() * 0.241 + c.getGreen() * c.getGreen() * 0.691 + c.getBlue() * c.getBlue() * 0.068);
        if (luminance > THRESHOLD) {
            return 0x404040;
        } else {
            return 0xffffff;
        }
    }

    @Override
    public MouseOverHelp getMouseOverHelp() {
        return mouseOverHelp;
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
        ModuleToggleButton(ModuleFlags setting, int x, int y, boolean toggled) {
            super(setting.ordinal(), x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled);
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

        DirectionButton(RelativeDirection dir, Module module, int x, int y, boolean toggled) {
            super(dir.ordinal() + DIRECTION_BASE_ID, DIRECTION_GROUP, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled);
            this.direction = dir;
            String dirStr = module.getDirectionString(dir);
            tooltip1.add(TextFormatting.GRAY + dirStr);
            tooltip2.add(TextFormatting.YELLOW + dirStr);
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
