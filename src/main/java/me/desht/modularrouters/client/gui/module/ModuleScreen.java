package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.gui.IMouseOverHelpProvider;
import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.FilterScreenFactory;
import me.desht.modularrouters.client.gui.widgets.button.*;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.logic.settings.*;
import me.desht.modularrouters.network.messages.ModuleSettingsMessage;
import me.desht.modularrouters.network.messages.OpenGuiMessage;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.Range;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ModuleScreen extends AbstractContainerScreen<ModuleMenu> implements ContainerListener, IMouseOverHelpProvider, ISendToServer {
    protected static final ResourceLocation GUI_TEXTURE = MiscUtil.RL("textures/gui/module.png");

    // locations of extra textures on the gui module texture sheet
    protected static final XYPoint SMALL_TEXTFIELD_XY = new XYPoint(0, 198);
    protected static final XYPoint LARGE_TEXTFIELD_XY = new XYPoint(0, 212);
    protected static final XYPoint BUTTON_XY = new XYPoint(0, 226);

    private static final int GUI_HEIGHT = 198;
    private static final int GUI_WIDTH = 192;
    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 16;

    protected final ItemStack moduleItemStack;
    private final ModuleItem module;
    private final BlockPos routerPos;
    private final int moduleSlotIndex;
    private final InteractionHand hand;
    private final ModuleSettings settings;
    private int sendDelay;
    private final MouseOverHelp mouseOverHelp;
    protected final AugmentItem.AugmentCounter augmentCounter;

    private int regulatorAmount;
    private RelativeDirection facing;
    private RedstoneBehaviourButton redstoneButton;
    private RegulatorTooltipButton regulatorTooltipButton;
    private final EnumMap<RelativeDirection,DirectionButton> directionButtons = new EnumMap<>(RelativeDirection.class);
    private MouseOverHelp.Button mouseOverHelpButton;
    private TexturedToggleButton matchAllButton;
    protected IntegerTextField regulatorTextField;
    private TerminationButton terminationButton;
    private ModuleToggleButton whiteListButton;
    private ModuleToggleButton matchDamageButton;
    private ModuleToggleButton matchCompButton;
    private ModuleToggleButton matchItemTagButton;

    public ModuleScreen(ModuleMenu container, Inventory inventory, Component displayName) {
        super(container, inventory, displayName);

        MFLocator locator = container.getLocator();
        moduleSlotIndex = locator.routerSlot();
        hand = locator.hand();
        routerPos = locator.routerPos();
        moduleItemStack = locator.getModuleStack(inventory.player);

        module = (ModuleItem) moduleItemStack.getItem();

        settings = ModuleItem.getCommonSettings(moduleItemStack);
        regulatorAmount = settings.regulatorAmount();
        facing = settings.facing();

        augmentCounter = new AugmentItem.AugmentCounter(moduleItemStack);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        mouseOverHelp = new MouseOverHelp(this);

        NeoForge.EVENT_BUS.addListener(this::onInitGui);
    }

    @Override
    public void init() {
        super.init();

        whiteListButton = addToggleButton(7, 75, settings.flags().whiteList(), "whitelist", 0, 32);
        matchDamageButton = addToggleButton(7, 93, settings.flags().matchDamage(), "match_damage", 32, 32);
        matchCompButton = addToggleButton(25, 75, settings.flags().matchComponents(), "match_components", 64, 32);
        matchItemTagButton = addToggleButton(25, 93, settings.flags().matchComponents(), "match_item_tag", 96, 32);
        matchAllButton = addToggleButton(45, 75, settings.flags().matchAllItems(), "match_all", 208, 16);

        terminationButton = addRenderableWidget(new TerminationButton(leftPos + 45, topPos + 93, settings.termination()));

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

        redstoneButton = addRenderableWidget(new RedstoneBehaviourButton(leftPos + 170, topPos + 93, BUTTON_WIDTH, BUTTON_HEIGHT,
                ModuleItem.getRedstoneBehaviour(moduleItemStack), this));

        regulatorTextField = addRenderableWidget(buildRegulationTextField());
        regulatorTooltipButton = addRenderableWidget(new RegulatorTooltipButton(regulatorTextField.getX() - 16, regulatorTextField.getY() - 2, module.isFluidModule()));

        if (routerPos != null) {
            addRenderableWidget(new BackButton(leftPos + 2, topPos + 1, p -> PacketDistributor.sendToServer(OpenGuiMessage.openRouter(menu.getLocator()))));
        }

        mouseOverHelp.addHelpRegion(leftPos + 7, topPos + 16, leftPos + 60, topPos + 69, "modularrouters.guiText.popup.filter");
        mouseOverHelp.addHelpRegion(leftPos + 5, topPos + 73, leftPos + 62, topPos + 110, "modularrouters.guiText.popup.filterControl");
        mouseOverHelp.addHelpRegion(leftPos + 68, topPos + 16, leftPos + 121, topPos + 69, module.isDirectional() ? "modularrouters.guiText.popup.direction" : "modularrouters.guiText.popup.noDirection");
        mouseOverHelp.addHelpRegion(leftPos + 77, topPos + 74, leftPos + 112, topPos + 109, "modularrouters.guiText.popup.augments");
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    protected IntegerTextField buildRegulationTextField() {
        IntegerTextField tf = new IntegerTextField(font, leftPos + 166, topPos + 75, 20, 12, Range.of(0, 64));
        tf.setValue(regulatorAmount);
        tf.setResponder((str) -> {
            regulatorAmount = str.isEmpty() ? 0 : Integer.parseInt(str);
            sendModuleSettingsDelayed(5);
        });
        return tf;
    }

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
        redstoneButton.visible = augmentCounter.getAugmentCount(ModItems.REDSTONE_AUGMENT) > 0;

        regulatorTooltipButton.visible = augmentCounter.getAugmentCount(ModItems.REGULATOR_AUGMENT) > 0;
        regulatorTextField.setVisible(regulatorTooltipButton.visible);
    }

    private ModuleToggleButton addToggleButton(int x, int y, boolean toggled, String name, int texX, int texY) {
        var btn = new ModuleToggleButton(this.leftPos + x, this.topPos + y, toggled, name, texX, texY);
        addRenderableWidget(btn);
        return btn;
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
    protected void sendModuleSettingsDelayed(int delay) {
        sendDelay = delay;
    }

    @Override
    public void sendToServer() {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        buildComponentPatch(builder);
        PacketDistributor.sendToServer(new ModuleSettingsMessage(menu.getLocator(), builder.build()));
    }

    /**
     * Build the updated item stack to be sent to the server. Components in this builder will be copied
     * into the server-side item, after validation.
     * <p>
     * Important: Overriding subclasses <strong>must</strong> call this superclass method!
     *
     * @param builder the data component patch builder
     */
    protected void buildComponentPatch(DataComponentPatch.Builder builder) {
        builder.set(ModDataComponents.COMMON_MODULE_SETTINGS.get(), new ModuleSettings(
                new ModuleFlags(
                        whiteListButton.isToggled(),
                        matchDamageButton.isToggled(),
                        matchCompButton.isToggled(),
                        matchItemTagButton.isToggled(),
                        matchAllButton.isToggled()
                ),
                facing,
                terminationButton.getState(),
                redstoneButton == null ? RedstoneBehaviour.ALWAYS : redstoneButton.getState(),
                regulatorAmount
        ));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component txt = moduleItemStack.getHoverName().copy().append(" ").append(
                routerPos != null ? xlate("modularrouters.guiText.label.installed") : Component.empty()
        );
        graphics.drawString(font, txt, this.imageWidth / 2 - font.width(txt) / 2, 5, getFgColor(module.getItemTint()), false);
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

    protected TintColor getGuiBackgroundTint() {
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
            PacketDistributor.sendToServer(OpenGuiMessage.openRouter(menu.getLocator()));
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
                PacketDistributor.sendToServer(OpenGuiMessage.openFilterInInstalledModule(locator));
            } else {
                // no container, just open the client-side GUI directly
                FilterScreenFactory.openFilterGui(locator);
            }
        } else if (hand != null) {
            // module is in player's hand
            MFLocator locator = MFLocator.filterInHeldModule(hand, filterSlotIndex);
            if (filter.hasMenu()) {
                PacketDistributor.sendToServer(OpenGuiMessage.openFilterInHeldModule(locator));
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

    protected Optional<ModularRouterBlockEntity> getItemRouter() {
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
            ClientUtil.setMultilineTooltip(this,
                    Component.translatable(isFluid ?  "modularrouters.guiText.tooltip.fluidRegulatorTooltip" : "modularrouters.guiText.tooltip.regulatorTooltip"),
                    Component.translatable("modularrouters.guiText.tooltip.numberFieldTooltip"));
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
        private final int texX;
        private final int texY;

        ModuleToggleButton(int x, int y, boolean toggled, String name, int texX, int texY) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, ModuleScreen.this);
            this.texX = texX;
            this.texY = texY;
            setTooltips(xlate("modularrouters.guiText.tooltip." + name + ".false"), xlate("modularrouters.guiText.tooltip." + name + ".true"));
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(isToggled() ? texX : texX + 16, texY);
        }
    }

    private class DirectionButton extends RadioButton {
        private static final int DIRECTION_GROUP = 1;
        private final RelativeDirection direction;

        DirectionButton(RelativeDirection dir, ModuleItem module, int x, int y, boolean toggled) {
            super(DIRECTION_GROUP, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, toggled, ModuleScreen.this);

            this.direction = dir;
            setTooltips(module.getDirectionString(dir).withStyle(ChatFormatting.GRAY), module.getDirectionString(dir).withStyle(ChatFormatting.YELLOW));
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

    private class TerminationButton extends TexturedCyclerButton<ModuleTermination> {
        public TerminationButton(int x, int y, ModuleTermination initialVal) {
            super(x, y, 16, 16, initialVal, ModuleScreen.this);
        }

        @Override
        protected Tooltip makeTooltip(ModuleTermination termination) {
            return Tooltip.create(xlate(termination.getTranslationKey() + ".header").append("\n").append(xlate(termination.getTranslationKey())));
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
