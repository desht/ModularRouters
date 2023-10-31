package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.gui.AbstractMRContainerScreen;
import me.desht.modularrouters.client.gui.IMouseOverHelpProvider;
import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.FilterScreenFactory;
import me.desht.modularrouters.client.gui.widgets.button.*;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.module.ModuleItem.ModuleFlags;
import me.desht.modularrouters.item.module.ModuleItem.RelativeDirection;
import me.desht.modularrouters.item.module.ModuleItem.Termination;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.Range;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class AbstractModuleScreen extends AbstractMRContainerScreen<ModuleMenu> implements ContainerListener, IMouseOverHelpProvider, ISendToServer {
    static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ModularRouters.MODID, "textures/gui/module.png");

    // locations of extra textures on the gui module texture sheet
    static final XYPoint SMALL_TEXTFIELD_XY = new XYPoint(0, 198);
    static final XYPoint LARGE_TEXTFIELD_XY = new XYPoint(0, 212);
    static final XYPoint BUTTON_XY = new XYPoint(0, 226);

    private static final int GUI_HEIGHT = 198;
    private static final int GUI_WIDTH = 192;
    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 16;

    final ItemStack moduleItemStack;
    private final ModuleItem module;
    private final BlockPos routerPos;
    private final int moduleSlotIndex;
    private final InteractionHand hand;
    private RelativeDirection facing;
    private int sendDelay;
    private int regulatorAmount;
    private final MouseOverHelp mouseOverHelp;
    final AugmentItem.AugmentCounter augmentCounter;
    private final boolean matchAll;

    private RedstoneBehaviourButton redstoneButton;
    private RegulatorTooltipButton regulatorTooltipButton;
    private final EnumMap<RelativeDirection,DirectionButton> directionButtons = new EnumMap<>(RelativeDirection.class);
    private final EnumMap<ModuleFlags,ModuleToggleButton> toggleButtons = new EnumMap<>(ModuleFlags.class);
    private MouseOverHelp.Button mouseOverHelpButton;
    private TexturedToggleButton matchAllButton;
    IntegerTextField regulatorTextField;
    private TerminationButton terminationButton;

    public AbstractModuleScreen(ModuleMenu container, Inventory inventory, Component displayName) {
        super(container, inventory, displayName);

        MFLocator locator = container.getLocator();
        this.moduleSlotIndex = locator.routerSlot;
        this.hand = locator.hand;
        this.routerPos = locator.routerPos;
        this.moduleItemStack = locator.getModuleStack(inventory.player);

        this.module = (ModuleItem) moduleItemStack.getItem();

        this.facing = ModuleHelper.getRelativeDirection(moduleItemStack);
        this.regulatorAmount = ModuleHelper.getRegulatorAmount(moduleItemStack);
        this.augmentCounter = new AugmentItem.AugmentCounter(moduleItemStack);
        this.matchAll = ModuleHelper.isMatchAll(moduleItemStack);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.mouseOverHelp = new MouseOverHelp(this);

        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void init() {
        super.init();

        addToggleButton(ModuleFlags.BLACKLIST, 7, 75);
        addToggleButton(ModuleFlags.IGNORE_DAMAGE, 7, 93);
        addToggleButton(ModuleFlags.IGNORE_NBT, 25, 75);
        addToggleButton(ModuleFlags.IGNORE_TAGS, 25, 93);
        terminationButton = addRenderableWidget(new TerminationButton(leftPos + 45, topPos + 93, ModuleHelper.getTermination(moduleItemStack)));
        matchAllButton = addRenderableWidget(new MatchAllButton(leftPos + 45, topPos + 75, matchAll));

        if (module.isDirectional()) {
            addDirectionButton(RelativeDirection.NONE, 70, 18);
            addDirectionButton(RelativeDirection.UP, 87, 18);
            addDirectionButton(RelativeDirection.LEFT, 70, 35);
            addDirectionButton(RelativeDirection.FRONT, 87, 35);
            addDirectionButton(RelativeDirection.RIGHT, 104, 35);
            addDirectionButton(RelativeDirection.DOWN, 87, 52);
            addDirectionButton(RelativeDirection.BACK, 104, 52);
        }

        mouseOverHelpButton = addRenderableWidget(new MouseOverHelp.Button(leftPos + 175, topPos + 1));

        redstoneButton = addRenderableWidget(new RedstoneBehaviourButton(this.leftPos + 170, this.topPos + 93, BUTTON_WIDTH, BUTTON_HEIGHT,
                ModuleHelper.getRedstoneBehaviour(moduleItemStack), this));

        regulatorTextField = addRenderableWidget(buildRegulationTextField(getOrCreateTextFieldManager()));
        regulatorTooltipButton = addRenderableWidget(new RegulatorTooltipButton(regulatorTextField.getX() - 16, regulatorTextField.getY() - 2, module.isFluidModule()));

        if (routerPos != null) {
            addRenderableWidget(new BackButton(leftPos + 2, topPos + 1, p -> PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openRouter(menu.getLocator()))));
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
    public void onInitGui(ScreenEvent.Init.Post event) {
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
        regulatorTextField.setVisible(regulatorTooltipButton.visible);
    }

    private void addToggleButton(ModuleFlags flag, int x, int y) {
        toggleButtons.put(flag, new ModuleToggleButton(flag, this.leftPos + x, this.topPos + y, ModuleHelper.checkFlag(moduleItemStack, flag)));
        addRenderableWidget(toggleButtons.get(flag));
    }

    private void addDirectionButton(RelativeDirection dir, int x, int y) {
        directionButtons.put(dir, new DirectionButton(dir, module, this.leftPos + x, this.topPos + y, dir == facing));
        addRenderableWidget(directionButtons.get(dir));
    }

    @Override
    public void containerTick() {
        super.containerTick();
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
    protected CompoundTag buildMessageData() {
        RouterRedstoneBehaviour behaviour = redstoneButton == null ? RouterRedstoneBehaviour.ALWAYS : redstoneButton.getState();
        CompoundTag compound = new CompoundTag();
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
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component title = moduleItemStack.getHoverName().copy().append(routerPos != null ? " " + I18n.get("modularrouters.guiText.label.installed") : "");
        graphics.drawString(font, title, this.imageWidth / 2 - font.width(title) / 2, 5, getFgColor(module.getItemTint()), false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        TintColor c = getGuiBackgroundTint();
        graphics.setColor(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0F);
        graphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (!module.isDirectional()) {
            graphics.blit(GUI_TEXTURE, leftPos + 69, topPos + 17, 204, 0, 52, 52);
        }
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    private TintColor getGuiBackgroundTint() {
        if (ConfigHolder.client.misc.moduleGuiBackgroundTint.get()) {
            TintColor c = module.getItemTint();
            float[] hsb = TintColor.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            return TintColor.getHSBColor(hsb[0], hsb[1] * 0.5f, hsb[2]);
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
        if (slot == null || !(slot.getItem().getItem() instanceof SmartFilterItem filter) || slot.index < 0 || slot.index >= Filter.FILTER_SIZE) {
            return false;
        }
        int filterSlotIndex = slot.index;
        if (routerPos != null) {
            // module is installed in a router
            MFLocator locator = MFLocator.filterInInstalledModule(routerPos, moduleSlotIndex, filterSlotIndex);
            if (filter.hasMenu()) {
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openFilterInInstalledModule(locator));
            } else {
                // no container, just open the client-side GUI directly
                FilterScreenFactory.openFilterGui(locator);
            }
        } else if (hand != null) {
            // module is in player's hand
            MFLocator locator = MFLocator.filterInHeldModule(hand, filterSlotIndex);
            if (filter.hasMenu()) {
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openFilterInHeldModule(locator));
            } else {
                // no container, just open the client-side GUI directly
                FilterScreenFactory.openFilterGui(locator);
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

    Optional<ModularRouterBlockEntity> getItemRouter() {
        return routerPos != null ? Minecraft.getInstance().level.getBlockEntity(routerPos, ModBlockEntities.MODULAR_ROUTER.get()) : Optional.empty();
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int slotInd, ItemStack stack) {
        if (slotInd >= ModuleMenu.AUGMENT_START && slotInd < ModuleMenu.AUGMENT_START + AugmentItem.SLOTS) {
            augmentCounter.refresh(moduleItemStack);
            setupButtonVisibility();
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int i1) {
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
        private static final XYPoint TEXTURE_XY = new XYPoint(112, 0);

        RegulatorTooltipButton(int x, int y, boolean isFluid) {
            super(x, y, 16, 16, p -> {});
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, isFluid ? "modularrouters.guiText.tooltip.fluidRegulatorTooltip" : "modularrouters.guiText.tooltip.regulatorTooltip");
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.numberFieldTooltip");
        }

        @Override
        protected XYPoint getTextureXY() {
            return TEXTURE_XY;
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
            // no sound
        }
    }

    private class ModuleToggleButton extends TexturedToggleButton {
        private final ModuleFlags flag;

        ModuleToggleButton(ModuleFlags flag, int x, int y, boolean toggled) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, AbstractModuleScreen.this);
            this.flag = flag;
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip." + flag + ".1");
            MiscUtil.appendMultilineText(tooltip2, ChatFormatting.WHITE, "modularrouters.guiText.tooltip." + flag + ".2");
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(flag.ordinal() * BUTTON_WIDTH * 2 + (isToggled() ? BUTTON_WIDTH : 0), flag.getTextureY());
        }
    }

    private class DirectionButton extends RadioButton {
        private static final int DIRECTION_GROUP = 1;
        private final RelativeDirection direction;

        DirectionButton(RelativeDirection dir, ModuleItem module, int x, int y, boolean toggled) {
            super(DIRECTION_GROUP, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, AbstractModuleScreen.this);

            this.direction = dir;
            tooltip1.add(module.getDirectionString(dir).withStyle(ChatFormatting.GRAY));
            tooltip2.add(module.getDirectionString(dir).withStyle(ChatFormatting.YELLOW));
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(direction.getTextureX(isToggled()), direction.getTextureY());
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
        private static final XYPoint TEXTURE_XY = new XYPoint(208, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(224, 16);

        MatchAllButton(int x, int y, boolean toggled) {
            super(x, y, 16, 16, toggled, AbstractModuleScreen.this);
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.matchAll.false");
            MiscUtil.appendMultilineText(tooltip2, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.matchAll.true");
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }

    private class TerminationButton extends TexturedCyclerButton<Termination> {
        private final List<List<Component>> tooltips = Lists.newArrayList();

        public TerminationButton(int x, int y, Termination initialVal) {
            super(x, y, 16, 16, initialVal, AbstractModuleScreen.this);

            for (Termination termination : Termination.values()) {
                List<Component> l = new ArrayList<>();
                l.add(xlate(termination.getTranslationKey() + ".header"));
                MiscUtil.appendMultilineText(l, ChatFormatting.GRAY, termination.getTranslationKey());
                tooltips.add(l);
            }
        }

        @Override
        public List<Component> getTooltipLines() {
            return tooltips.get(getState().ordinal());
        }

        @Override
        protected XYPoint getTextureXY() {
            int x = switch (getState()) {
                case NONE -> 128;
                case NOT_RAN -> 224;
                case RAN -> 144;
            };
            return new XYPoint(x, 32);
        }
    }
}
