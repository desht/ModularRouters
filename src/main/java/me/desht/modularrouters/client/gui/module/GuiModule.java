package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.gui.IMouseOverHelpProvider;
import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.FilterGuiFactory;
import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.client.gui.widgets.button.*;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleFlags;
import me.desht.modularrouters.item.module.ItemModule.RelativeDirection;
import me.desht.modularrouters.item.module.ItemModule.Termination;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.Range;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

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
    private final boolean matchAll;

    private RedstoneBehaviourButton redstoneButton;
    private RegulatorTooltipButton regulatorTooltipButton;
    private final EnumMap<RelativeDirection,DirectionButton> directionButtons = new EnumMap<>(RelativeDirection.class);
    private final EnumMap<ModuleFlags,ModuleToggleButton> toggleButtons = new EnumMap<>(ModuleFlags.class);
    private MouseOverHelp.Button mouseOverHelpButton;
    private TexturedToggleButton matchAllButton;
    IntegerTextField regulatorTextField;
    private TerminationButton terminationButton;

    public GuiModule(ContainerModule container, PlayerInventory inventory, ITextComponent displayName) {
        super(container, inventory, displayName);

        MFLocator locator = container.getLocator();
        this.moduleSlotIndex = locator.routerSlot;
        this.hand = locator.hand;
        this.routerPos = locator.routerPos;
        this.moduleItemStack = locator.getModuleStack(inventory.player);

        this.module = (ItemModule) moduleItemStack.getItem();

        this.facing = ModuleHelper.getRelativeDirection(moduleItemStack);
        this.regulatorAmount = ModuleHelper.getRegulatorAmount(moduleItemStack);
        this.augmentCounter = new ItemAugment.AugmentCounter(moduleItemStack);
        this.matchAll = ModuleHelper.isMatchAll(moduleItemStack);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.mouseOverHelp = new MouseOverHelp(this);

        this.passEvents = true;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void init() {
        super.init();

        addToggleButton(ModuleFlags.BLACKLIST, 7, 75);
        addToggleButton(ModuleFlags.IGNORE_DAMAGE, 7, 93);
        addToggleButton(ModuleFlags.IGNORE_NBT, 25, 75);
        addToggleButton(ModuleFlags.IGNORE_TAGS, 25, 93);
//        addToggleButton(ModuleFlags.TERMINATE, 45, 93);
        terminationButton = addButton(new TerminationButton(leftPos + 45, topPos + 93, ModuleHelper.getTermination(moduleItemStack)));
        addButton(matchAllButton = new MatchAllButton(leftPos + 45, topPos + 75, matchAll));

        if (module.isDirectional()) {
            addDirectionButton(RelativeDirection.NONE, 70, 18);
            addDirectionButton(RelativeDirection.UP, 87, 18);
            addDirectionButton(RelativeDirection.LEFT, 70, 35);
            addDirectionButton(RelativeDirection.FRONT, 87, 35);
            addDirectionButton(RelativeDirection.RIGHT, 104, 35);
            addDirectionButton(RelativeDirection.DOWN, 87, 52);
            addDirectionButton(RelativeDirection.BACK, 104, 52);
        }

        addButton(mouseOverHelpButton = new MouseOverHelp.Button(leftPos + 175, topPos + 1));

        addButton(redstoneButton = new RedstoneBehaviourButton(this.leftPos + 170, this.topPos + 93, BUTTON_WIDTH, BUTTON_HEIGHT,
                ModuleHelper.getRedstoneBehaviour(moduleItemStack), this));

        addButton(regulatorTextField = buildRegulationTextField(getOrCreateTextFieldManager()));

        addButton(regulatorTooltipButton = new RegulatorTooltipButton(regulatorTextField.x - 16, regulatorTextField.y - 2, module.isFluidModule()));

        if (routerPos != null) {
            addButton(new BackButton(leftPos + 2, topPos + 1, p -> PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openRouter(menu.getLocator()))));
        }

        mouseOverHelp.addHelpRegion(leftPos + 7, topPos + 16, leftPos + 60, topPos + 69, "modularrouters.guiText.popup.filter");
        mouseOverHelp.addHelpRegion(leftPos + 5, topPos + 73, leftPos + 62, topPos + 110, "modularrouters.guiText.popup.filterControl");
        mouseOverHelp.addHelpRegion(leftPos + 68, topPos + 16, leftPos + 121, topPos + 69, module.isDirectional() ? "modularrouters.guiText.popup.direction" : "modularrouters.guiText.popup.noDirection");
        mouseOverHelp.addHelpRegion(leftPos + 77, topPos + 74, leftPos + 112, topPos + 109, "modularrouters.guiText.popup.augments");
    }

    protected IntegerTextField buildRegulationTextField(TextFieldManager manager) {
        IntegerTextField tf = new IntegerTextField(manager, font, leftPos + 166, topPos + 75, 20, 12, Range.between(0, 64));
        tf.setValue(regulatorAmount);
        tf.setResponder((str) -> {
            regulatorAmount = str.isEmpty() ? 0 : Integer.parseInt(str);
            sendModuleSettingsDelayed(5);
        });
        return tf;
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        getMenu().removeSlotListener(this);
        getMenu().addSlotListener(this);
        setupButtonVisibility();
    }

    public int getRegulatorAmount() {
        return regulatorAmount;
    }

    public void setRegulatorAmount(int regulatorAmount) {
        this.regulatorAmount = regulatorAmount;
    }

    protected void setupButtonVisibility() {
        redstoneButton.visible = augmentCounter.getAugmentCount(ModItems.REDSTONE_AUGMENT.get()) > 0;
        regulatorTooltipButton.visible = augmentCounter.getAugmentCount(ModItems.REGULATOR_AUGMENT.get()) > 0;
        regulatorTextField.setVisible(augmentCounter.getAugmentCount(ModItems.REGULATOR_AUGMENT.get()) > 0);
    }

    private void addToggleButton(ModuleFlags flag, int x, int y) {
        toggleButtons.put(flag, new ModuleToggleButton(flag, this.leftPos + x, this.topPos + y, ModuleHelper.checkFlag(moduleItemStack, flag)));
        addButton(toggleButtons.get(flag));
    }

    private void addDirectionButton(RelativeDirection dir, int x, int y) {
        directionButtons.put(dir, new DirectionButton(dir, module, this.leftPos + x, this.topPos + y, dir == facing));
        addButton(directionButtons.get(dir));
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
        PacketHandler.NETWORK.sendToServer(new ModuleSettingsMessage(menu.getLocator(), buildMessageData()));
    }

    /**
     * Encode the message data to be sent to the server in {@link ModuleSettingsMessage}. This NBT data will
     * be copied directly into the module itemstack's NBT when the server receives the packet.
     * Overriding subclasses must call the superclass method!
     *
     * @return the message data NBT
     */
    protected CompoundNBT buildMessageData() {
        RouterRedstoneBehaviour behaviour = redstoneButton == null ? RouterRedstoneBehaviour.ALWAYS : redstoneButton.getState();
        CompoundNBT compound = new CompoundNBT();
        for (ModuleFlags flag : ModuleFlags.values()) {
            compound.putBoolean(flag.getName(), toggleButtons.get(flag).isToggled());
        }
        compound.putString(ModuleHelper.NBT_TERMINATION, terminationButton.getState().toString());
        compound.putString(ModuleHelper.NBT_DIRECTION, facing.toString());
        compound.putByte(ModuleHelper.NBT_REDSTONE_MODE, (byte) behaviour.ordinal());
        compound.putInt(ModuleHelper.NBT_REGULATOR_AMOUNT, regulatorAmount);
        compound.putBoolean(ModuleHelper.NBT_MATCH_ALL, matchAllButton.isToggled());
        return compound;
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        ITextComponent title = moduleItemStack.getHoverName().copy().append(routerPos != null ? " " + I18n.get("modularrouters.guiText.label.installed") : "");
        this.font.draw(matrixStack, title, this.imageWidth / 2f - this.font.width(title) / 2f, 5, getFgColor(module.getItemTint()));
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        TintColor c = getGuiBackgroundTint();
        RenderSystem.color4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0F);
        minecraft.getTextureManager().bind(GUI_TEXTURE);
        blit(matrixStack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (!module.isDirectional()) {
            blit(matrixStack, leftPos + 69, topPos + 17, 204, 0, 52, 52);
        }
    }

    private TintColor getGuiBackgroundTint() {
        if (MRConfig.Client.Misc.moduleGuiBackgroundTint) {
            TintColor c = module.getItemTint();
            float[] hsb = TintColor.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            return TintColor.getHSBColor(hsb[0], hsb[1] * 0.7f, hsb[2]);
        } else {
            return TintColor.WHITE;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ESCAPE || (ClientUtil.isInvKey(keyCode) && !isFocused())) && routerPos != null) {
            // Intercept ESC/E and immediately reopen the router GUI - this avoids an
            // annoying screen flicker between closing the module GUI and reopen the router GUI.
            // Sending the reopen message will also close this gui, triggering onGuiClosed()
            PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openRouter(menu.getLocator()));
            return true;
        } else if (ClientSetup.keybindConfigure.getKey().getValue() == keyCode) {
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
        if (slot == null || !(slot.getItem().getItem() instanceof ItemSmartFilter) || slot.index < 0 || slot.index >= Filter.FILTER_SIZE) {
            return false;
        }
        int filterSlotIndex = slot.index;
        ItemSmartFilter filter = (ItemSmartFilter) slot.getItem().getItem();
        if (routerPos != null) {
            // module is installed in a router
            MFLocator locator = MFLocator.filterInInstalledModule(routerPos, moduleSlotIndex, filterSlotIndex);
            if (filter.hasContainer()) {
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openFilterInInstalledModule(locator));
            } else {
                // no container, just open the client-side GUI directly
                FilterGuiFactory.openFilterGui(locator);
            }
        } else if (hand != null) {
            // module is in player's hand
            MFLocator locator = MFLocator.filterInHeldModule(hand, filterSlotIndex);
            if (filter.hasContainer()) {
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openFilterInHeldModule(locator));
            } else {
                // no container, just open the client-side GUI directly
                FilterGuiFactory.openFilterGui(locator);
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
        return routerPos != null ? TileEntityItemRouter.getRouterAt(Minecraft.getInstance().level, routerPos) : Optional.empty();
    }

    @Override
    public void refreshContainer(Container containerToSend, NonNullList<ItemStack> itemsList) {
    }

    @Override
    public void slotChanged(Container containerToSend, int slotInd, ItemStack stack) {
        if (slotInd >= ContainerModule.AUGMENT_START && slotInd < ContainerModule.AUGMENT_START + ItemAugment.SLOTS) {
            augmentCounter.refresh(moduleItemStack);
            setupButtonVisibility();
        }
    }

    @Override
    public void setContainerData(Container containerIn, int varToUpdate, int newValue) {
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
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, isFluid ? "modularrouters.guiText.tooltip.fluidRegulatorTooltip" : "modularrouters.guiText.tooltip.regulatorTooltip");
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "modularrouters.guiText.tooltip.numberFieldTooltip");
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
        private final ModuleFlags flag;

        ModuleToggleButton(ModuleFlags flag, int x, int y, boolean toggled) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, GuiModule.this);
            this.flag = flag;
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "modularrouters.guiText.tooltip." + flag + ".1");
            MiscUtil.appendMultilineText(tooltip2, TextFormatting.WHITE, "modularrouters.guiText.tooltip." + flag + ".2");
        }

        @Override
        protected int getTextureX() {
            return flag.ordinal() * BUTTON_WIDTH * 2 + (isToggled() ? BUTTON_WIDTH : 0);
        }

        @Override
        protected int getTextureY() {
            return flag.getTextureY();
        }
    }

    private class DirectionButton extends RadioButton {
        private static final int DIRECTION_GROUP = 1;
        private final RelativeDirection direction;

        DirectionButton(RelativeDirection dir, ItemModule module, int x, int y, boolean toggled) {
            super(DIRECTION_GROUP, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, GuiModule.this);

            this.direction = dir;
            StringTextComponent dirStr = new StringTextComponent(module.getDirectionString(dir));
            tooltip1.add(dirStr.withStyle(TextFormatting.GRAY));
            tooltip2.add(dirStr.withStyle(TextFormatting.YELLOW));
        }

        @Override
        protected int getTextureX() {
            return direction.getTextureX(isToggled());
        }

        @Override
        protected int getTextureY() {
            return direction.getTextureY();
        }

        public RelativeDirection getDirection() {
            return direction;
        }

        @Override
        public void onPress() {
            for (RelativeDirection dir : RelativeDirection.values()) {
                DirectionButton db = directionButtons.get(dir);
                db.setToggled(false);
                if (db == this) {
                    facing = db.getDirection();
                }
            }

            super.onPress();
        }
    }

    private class MatchAllButton extends TexturedToggleButton {
        MatchAllButton(int x, int y, boolean toggled) {
            super(x, y, 16, 16, toggled, GuiModule.this);
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "modularrouters.guiText.tooltip.matchAll.false");
            MiscUtil.appendMultilineText(tooltip2, TextFormatting.WHITE, "modularrouters.guiText.tooltip.matchAll.true");
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 224 : 208;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }

    private class TerminationButton extends TexturedCyclerButton<Termination> {
        private final List<List<ITextComponent>> tooltips = Lists.newArrayList();

        public TerminationButton(int x, int y, Termination initialVal) {
            super(x, y, 16, 16, initialVal, GuiModule.this);

            for (Termination termination : Termination.values()) {
                List<ITextComponent> l = new ArrayList<>();
                l.add(xlate(termination.getTranslationKey() + ".header"));
                MiscUtil.appendMultilineText(l, TextFormatting.GRAY, termination.getTranslationKey());
                tooltips.add(l);
            }
        }

        @Override
        public List<ITextComponent> getTooltip() {
            return tooltips.get(getState().ordinal());
        }

        @Override
        protected int getTextureX() {
            switch (getState()) {
                case NONE: return 128;
                case NOT_RAN: return 224;
                case RAN: return 144;
                default: throw new IllegalArgumentException("unknown value");
            }
        }

        @Override
        protected int getTextureY() {
            return 32;
        }
    }
}
