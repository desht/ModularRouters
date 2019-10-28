package me.desht.modularrouters.client.gui.module;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.gui.IMouseOverHelpProvider;
import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.FilterGuiFactory;
import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.client.gui.widgets.button.*;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleFlags;
import me.desht.modularrouters.item.module.ItemModule.RelativeDirection;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.Range;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class GuiModule extends GuiContainerBase<ContainerModule> implements IContainerListener, IMouseOverHelpProvider, ISendToServer {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ModularRouters.MODID, "textures/gui/module.png");

    // locations of extra textures on the gui module texture sheet
    static final XYPoint SMALL_TEXTFIELD_XY = new XYPoint(0, 198);
    static final XYPoint LARGE_TEXTFIELD_XY = new XYPoint(0, 212);
    static final XYPoint BUTTON_XY = new XYPoint(0, 226);

    private static final int GUI_HEIGHT = 198;
    private static final int GUI_WIDTH = 192;
    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 16;

    final ItemStack moduleItemStack;
    private final ItemModule module;
    private final BlockPos routerPos;
    private final int moduleSlotIndex;
    private final Hand hand;
    private RelativeDirection facing;
    private int sendDelay;
    private int regulatorAmount;
    private final MouseOverHelp mouseOverHelp;
    final ItemAugment.AugmentCounter augmentCounter;

    private RedstoneBehaviourButton redstoneButton;
    private RegulatorTooltipButton regulatorTooltipButton;
    private DirectionButton[] directionButtons = new DirectionButton[RelativeDirection.values().length];
    private ModuleToggleButton[] toggleButtons = new ModuleToggleButton[ModuleFlags.values().length];
    private MouseOverHelp.Button mouseOverHelpButton;
    IntegerTextField regulatorTextField;

    public GuiModule(ContainerModule container, PlayerInventory inventory, ITextComponent displayName) {
        super(container, inventory, displayName);

        MFLocator locator = container.getLocator();
        this.moduleSlotIndex = locator.routerSlot;
        this.hand = locator.hand;
        this.routerPos = locator.routerPos;
        this.moduleItemStack = locator.getModuleStack(inventory.player);

        this.module = (ItemModule) moduleItemStack.getItem();

        this.facing = ModuleHelper.getDirectionFromNBT(moduleItemStack);
        this.regulatorAmount = ModuleHelper.getRegulatorAmount(moduleItemStack);
        this.augmentCounter = new ItemAugment.AugmentCounter(moduleItemStack);
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        this.mouseOverHelp = new MouseOverHelp(this);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void init() {
        super.init();

        addToggleButton(ModuleFlags.BLACKLIST, 7, 75);
        addToggleButton(ModuleFlags.IGNORE_DAMAGE, 7, 93);
        addToggleButton(ModuleFlags.IGNORE_NBT, 25, 75);
        addToggleButton(ModuleFlags.IGNORE_TAGS, 25, 93);
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

        addButton(mouseOverHelpButton = new MouseOverHelp.Button(guiLeft + 175, guiTop + 1, mouseOverHelp));

        addButton(redstoneButton = new RedstoneBehaviourButton(this.guiLeft + 170, this.guiTop + 93, BUTTON_WIDTH, BUTTON_HEIGHT,
                ModuleHelper.getRedstoneBehaviour(moduleItemStack), this));

        TextFieldManager manager = createTextFieldManager();
        Range<Integer> range = module.isFluidModule() ? Range.between(0, 100) : Range.between(0, 64);
        int xOff = module.isFluidModule() ? 0 : 10;
        regulatorTextField = new IntegerTextField(manager, font,
                guiLeft + 156 + xOff, guiTop + 75, 20, 12, range.getMinimum(), range.getMaximum());
        regulatorTextField.setValue(regulatorAmount);
        regulatorTextField.setResponder((str) -> {
            regulatorAmount = str.isEmpty() ? 0 : Integer.parseInt(str);
            sendModuleSettingsDelayed(5);
        });

        addButton(regulatorTooltipButton = new RegulatorTooltipButton(guiLeft + 138 + xOff, guiTop + 73, module.isFluidModule()));

        if (routerPos != null) {
            addButton(new BackButton(guiLeft + 2, guiTop + 1, p -> PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openRouter(container.getLocator()))));
        }

        mouseOverHelp.addHelpRegion(guiLeft + 7, guiTop + 16, guiLeft + 60, guiTop + 69, "guiText.popup.filter");
        mouseOverHelp.addHelpRegion(guiLeft + 5, guiTop + 73, guiLeft + 62, guiTop + 110, "guiText.popup.filterControl");
        mouseOverHelp.addHelpRegion(guiLeft + 68, guiTop + 16, guiLeft + 121, guiTop + 69, module.isDirectional() ? "guiText.popup.direction" : "guiText.popup.noDirection");
        mouseOverHelp.addHelpRegion(guiLeft + 77, guiTop + 74, guiLeft + 112, guiTop + 109, "guiText.popup.augments");
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        getContainer().removeListener(this);
        getContainer().addListener(this);
        setupButtonVisibility();
    }

    protected void setupButtonVisibility() {
        redstoneButton.visible = augmentCounter.getAugmentCount(ModItems.REDSTONE_AUGMENT) > 0;
        regulatorTooltipButton.visible = augmentCounter.getAugmentCount(ModItems.REGULATOR_AUGMENT) > 0;
        regulatorTextField.setVisible(augmentCounter.getAugmentCount(ModItems.REGULATOR_AUGMENT) > 0);
    }

    private void addToggleButton(ModuleFlags flag, int x, int y) {
        toggleButtons[flag.ordinal()] = new ModuleToggleButton(flag, this.guiLeft + x, this.guiTop + y, ModuleHelper.checkFlag(moduleItemStack, flag));
        addButton(toggleButtons[flag.ordinal()]);
    }

    private void addDirectionButton(RelativeDirection dir, int x, int y) {
        directionButtons[dir.ordinal()] = new DirectionButton(dir, module, this.guiLeft + x, this.guiTop + y, dir == facing);
        addButton(directionButtons[dir.ordinal()]);
    }

    @Override
    public void tick() {
        super.tick();
        mouseOverHelp.setActive(mouseOverHelpButton.isToggled());
        if (sendDelay > 0 && --sendDelay <= 0) {
            sendToServer();
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

    @Override
    public void sendToServer() {
        PacketHandler.NETWORK.sendToServer(new ModuleSettingsMessage(container.getLocator(), buildMessageData()));
    }

    /**
     * Encode the message data for this module.  This NBT data will be copied directly
     * into the module itemstack's NBT when the server receives the module settings message.
     * Overriding subclasses must call the superclass method!
     *
     * @return the message data NBT
     */
    protected CompoundNBT buildMessageData() {
        byte flags = (byte) (facing.ordinal() << 4);
        for (ModuleFlags setting : ModuleFlags.values()) {
            if (getToggleButton(setting).isToggled()) {
                flags |= setting.getMask();
            }
        }
        RouterRedstoneBehaviour behaviour = redstoneButton == null ? RouterRedstoneBehaviour.ALWAYS : redstoneButton.getState();
        CompoundNBT compound = new CompoundNBT();
        compound.putByte(ModuleHelper.NBT_FLAGS, flags);
        compound.putByte(ModuleHelper.NBT_REDSTONE_MODE, (byte) behaviour.ordinal());
        compound.putInt(ModuleHelper.NBT_REGULATOR_AMOUNT, regulatorAmount);
        return compound;
    }

    private ModuleToggleButton getToggleButton(ModuleFlags flags) {
        return toggleButtons[flags.ordinal()];
    }

    private DirectionButton getDirectionButton(RelativeDirection direction) {
        return directionButtons[direction.ordinal()];
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        String title = moduleItemStack.getDisplayName().getString() + (routerPos != null ? " " + I18n.format("guiText.label.installed") : "");
        this.font.drawString(title, this.xSize / 2f - this.font.getStringWidth(title) / 2f, 5, getFgColor(module.getItemTint()));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        TintColor c = getGuiBackgroundTint();
        GlStateManager.color4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0F);
        minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        blit(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        if (!module.isDirectional()) {
            blit(guiLeft + 69, guiTop + 17, 204, 0, 52, 52);
        }
    }

    private TintColor getGuiBackgroundTint() {
        if (ConfigHandler.CLIENT_MISC.moduleGuiBackgroundTint.get()) {
            TintColor c = module.getItemTint();
            float[] hsb = TintColor.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            return TintColor.getHSBColor(hsb[0], hsb[1] * 0.7f, hsb[2]);
        } else {
            return TintColor.WHITE;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ESCAPE || (keyCode == GLFW.GLFW_KEY_E && !isFocused())) && routerPos != null) {
            // Intercept ESC/E and immediately reopen the router GUI - this avoids an
            // annoying screen flicker between closing the module GUI and reopen the router GUI.
            // Sending the reopen message will also close this gui, triggering onGuiClosed()
            PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openRouter(container.getLocator()));
            return true;
        } else if (Keybindings.keybindConfigure.getKey().getKeyCode() == keyCode) {
            // trying to configure an installed smart filter, we're done
            return handleFilterConfig();
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return button == 2 ? handleFilterConfig() : super.mouseClicked(x, y, button);
    }

    private boolean handleFilterConfig() {
        Slot slot = getSlotUnderMouse();
        if (slot == null || !(slot.getStack().getItem() instanceof ItemSmartFilter) || slot.slotNumber < 0 || slot.slotNumber >= Filter.FILTER_SIZE) {
            return false;
        }
        int filterSlotIndex = slot.slotNumber;
        ItemSmartFilter filter = (ItemSmartFilter) slot.getStack().getItem();
        if (routerPos != null) {
            // module is installed in a router
            MFLocator locator = MFLocator.filterInInstalledModule(routerPos, moduleSlotIndex, filterSlotIndex);
            if (filter.hasContainer()) {
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openFilterInInstalledModule(locator));
            } else {
                // no container, just open the client-side GUI directly
                minecraft.displayGuiScreen(FilterGuiFactory.createGuiForFilter(locator));
            }
        } else if (hand != null) {
            // module is in player's hand
            MFLocator locator = MFLocator.filterInHeldModule(hand, filterSlotIndex);
            if (filter.hasContainer()) {
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openFilterInHeldModule(locator));
            } else {
                // no container, just open the client-side GUI directly
                minecraft.displayGuiScreen(FilterGuiFactory.createGuiForFilter(locator));
            }
        }
        return true;
    }

    @Override
    public void removed() {
        super.removed();
        if (sendDelay > 0) {
            sendToServer();  // ensure no delayed updates get lost
        }
    }

    Optional<TileEntityItemRouter> getItemRouter() {
        return routerPos != null ? TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, routerPos) : Optional.empty();
    }

    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
    }

    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if (slotInd >= ContainerModule.AUGMENT_START && slotInd < ContainerModule.AUGMENT_START + ItemAugment.SLOTS) {
            augmentCounter.refresh(moduleItemStack);
            setupButtonVisibility();
        }
    }

    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
    }

    private static final int THRESHOLD = 129;
    private int getFgColor(TintColor bg) {
        // calculate a foreground color which suitably contrasts with the given background color
        int luminance = (int) Math.sqrt(
                bg.getRed() * bg.getRed() * 0.241 +
                bg.getGreen() * bg.getGreen() * 0.691 +
                bg.getBlue() * bg.getBlue() * 0.068
        );
        return luminance > THRESHOLD ? 0x404040 : 0xffffff;
    }

    @Override
    public MouseOverHelp getMouseOverHelp() {
        return mouseOverHelp;
    }

    private static class RegulatorTooltipButton extends TexturedButton {
        RegulatorTooltipButton(int x, int y, boolean isFluid) {
            super(x, y, 16, 16, p -> {});
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
        public void playDownSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }

    private class ModuleToggleButton extends TexturedToggleButton {
        private final int flagId;

        ModuleToggleButton(ModuleFlags flag, int x, int y, boolean toggled) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, GuiModule.this);
            this.flagId = flag.ordinal();
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip." + ModuleFlags.values()[flagId] + ".1");
            MiscUtil.appendMultiline(tooltip2, "guiText.tooltip." + ModuleFlags.values()[flagId] + ".2");
        }

        @Override
        protected int getTextureX() {
            return this.flagId * BUTTON_WIDTH * 2 + (isToggled() ? BUTTON_WIDTH : 0);
        }

        @Override
        protected int getTextureY() {
            return 32;
        }
    }

    private class DirectionButton extends RadioButton {
        private static final int DIRECTION_GROUP = 1;
        private final RelativeDirection direction;

        DirectionButton(RelativeDirection dir, ItemModule module, int x, int y, boolean toggled) {
            super(DIRECTION_GROUP, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, GuiModule.this);

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

        @Override
        public void onPress() {
            for (RelativeDirection dir : RelativeDirection.values()) {
                DirectionButton db = getDirectionButton(dir);
                db.setToggled(false);
                if (db == this) {
                    facing = db.getDirection();
                }
            }

            super.onPress();
        }
    }
}
