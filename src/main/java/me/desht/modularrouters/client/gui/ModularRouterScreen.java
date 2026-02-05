package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.KeyBindings;
import me.desht.modularrouters.client.gui.widgets.EnergyWidget;
import me.desht.modularrouters.client.gui.widgets.button.RedstoneBehaviourButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.RouterMenu;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.network.messages.OpenGuiMessage;
import me.desht.modularrouters.network.messages.RouterSettingsMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModularRouterScreen extends AbstractContainerScreen<RouterMenu> implements ISendToServer, MenuAccess<RouterMenu> {
    private static final Identifier TEXTURE_LOCATION = RL("textures/gui/router.png");

    private static final int LABEL_YPOS = 5;
    private static final int MODULE_LABEL_YPOS = 60;
    private static final int BUFFER_LABEL_YPOS = 28;
    private static final int UPGRADES_LABEL_YPOS = 28;
    private static final int GUI_HEIGHT = 186;
    private static final int GUI_WIDTH = 176;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 16;

    private static final int MODULE_START = RouterMenu.TE_FIRST_SLOT + RouterMenu.MODULE_SLOT_START;
    private static final int MODULE_END = RouterMenu.TE_FIRST_SLOT + RouterMenu.MODULE_SLOT_END;

    private RedstoneBehaviourButton redstoneBehaviourButton;
    private EcoButton ecoButton;
    private EnergyDirectionButton energyDirButton;
    private EnergyWidget energyWidget;
    private EnergyWarningButton energyWarning;
    private int energyUsage;

    public ModularRouterScreen(RouterMenu container, Inventory inventoryPlayer, Component displayName) {
        super(container, inventoryPlayer, displayName);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        ModularRouterBlockEntity router = menu.getRouter();

        addRenderableWidget(redstoneBehaviourButton = new RedstoneBehaviourButton(this.leftPos + 152, this.topPos + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getRedstoneBehaviour(), this));
        addRenderableWidget(ecoButton = new EcoButton(this.leftPos + 132, this.topPos + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getEcoMode()));
        addRenderableWidget(energyDirButton = new EnergyDirectionButton(this.leftPos - 8, this.topPos + 40, router.getEnergyDirection()));
        addRenderableWidget(energyWidget = new EnergyWidget(this.leftPos - 22, this.topPos + 15, (EnergyHandler) router.getEnergyStorage()));
        addRenderableWidget(energyWarning = new EnergyWarningButton(this.leftPos + 4, this.topPos + 4));
        energyWidget.visible = energyDirButton.visible = router.getEnergyCapacity() > 0;
        energyWarning.visible = false;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MutableComponent title = xlate("block.modularrouters.modular_router");
        graphics.drawString(font, title, this.imageWidth / 2 - font.width(title) / 2, LABEL_YPOS, 0xFF404040, false);
        graphics.drawString(font, xlate("modularrouters.guiText.label.buffer"), 8, BUFFER_LABEL_YPOS, 0xFF404040, false);
        graphics.drawString(font, xlate("modularrouters.guiText.label.upgrades"), RouterMenu.UPGRADE_XPOS, UPGRADES_LABEL_YPOS, 0xFF404040, false);
        graphics.drawString(font, xlate("modularrouters.guiText.label.modules"), RouterMenu.MODULE_XPOS, MODULE_LABEL_YPOS, 0xFF404040, false);
        graphics.drawString(font, xlate("container.inventory"), 8, this.imageHeight - 96 + 4, 0xFF404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float v, int i, int i1) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.getRouter().getEnergyCapacity() > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, leftPos - 27, topPos, 180, 0, 32, 100, 256, 256);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return KeyBindings.keybindConfigure.getKey().getValue() == event.key() ? handleModuleConfig() : super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean flag) {
        return event.button() == 2 ? handleModuleConfig() : super.mouseClicked(event, flag);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        energyUsage = 0;
        for (int i = MODULE_START; i < MODULE_END; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.getItem() instanceof ModuleItem moduleItem) {
                energyUsage += moduleItem.getEnergyCost(stack);
            }
        }

        boolean hasEnergyUpgrade = menu.getRouter().getEnergyCapacity() > 0;
        energyWidget.visible = hasEnergyUpgrade;
        ItemStack bufferStack = getMenu().getSlot(RouterMenu.TE_FIRST_SLOT).getItem();
        energyDirButton.visible = hasEnergyUpgrade
                && !bufferStack.isEmpty()
                && bufferStack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(bufferStack)) != null;

        energyWarning.setX(hasEnergyUpgrade ? leftPos - 22 : leftPos + 4);
        energyWarning.tick();
    }

    private boolean handleModuleConfig() {
        Slot slot = getSlotUnderMouse();
        if (slot == null || !(slot.getItem().getItem() instanceof ModuleItem) || slot.index < MODULE_START || slot.index > MODULE_END) {
            return false;
        }
        MFLocator locator = MFLocator.moduleInRouter(menu.getRouter().getBlockPos(), slot.index - MODULE_START);
        ClientPacketDistributor.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
        return true;
    }

    @Override
    public void sendToServer() {
        ModularRouterBlockEntity router = menu.getRouter();

        router.setRedstoneBehaviour(redstoneBehaviourButton.getState());
        router.setEcoMode(ecoButton.isToggled());
        router.setEnergyDirection(energyDirButton.getState());
        ClientPacketDistributor.sendToServer(RouterSettingsMessage.forRouter(router));
    }

    public List<Rect2i> getExtraArea() {
        // for JEI's benefit
        return menu.getRouter().getEnergyCapacity() > 0 ?
                List.of(new Rect2i(leftPos - 27, topPos, 32, 100)) :
                List.of();
    }

    private class EcoButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(80, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(96, 16);

        EcoButton(int x, int y, int width, int height, boolean initialVal) {
            super(x, y, width, height, initialVal, ModularRouterScreen.this);
            setTooltips(
                    xlate("modularrouters.guiText.tooltip.eco.false"),
                    xlate("modularrouters.guiText.tooltip.eco.true",
                            ConfigHolder.common.router.ecoTimeout.get() / 20.f,
                            ConfigHolder.common.router.lowPowerTickRate.get() / 20.f)
            );
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }

    private class EnergyDirectionButton extends TexturedCyclerButton<ModularRouterBlockEntity.EnergyDirection> {
        public EnergyDirectionButton(int x, int y, ModularRouterBlockEntity.EnergyDirection initialVal) {
            super(x, y, 14, 14, initialVal, ModularRouterScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            int x = switch (getState()) {
                case TO_ROUTER -> 224;
                case FROM_ROUTER -> 144;
                case NONE -> 176;
            };
            return new XYPoint(x, 0);
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }
    }

    private class EnergyWarningButton extends TexturedButton {
        private EnergyStatus prevStatus;

        public EnergyWarningButton(int x, int y) {
            super(x, y, 16, 16, b -> {});
        }

        private void tick() {
            EnergyStatus status;
            if (energyUsage <= ((EnergyHandler) menu.getRouter().getEnergyStorage()).getAmountAsInt()) {
                status = EnergyStatus.OK;
            } else if (menu.getRouter().getEnergyCapacity() > 0) {
                status = EnergyStatus.ENERGY_LOW;
            } else {
                status = EnergyStatus.NO_UPGRADES;
            }
            if (status != prevStatus) {
                setTooltip(status.getTooltip());
                prevStatus = status;
            }
            visible = status != EnergyStatus.OK;
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(240, Minecraft.getInstance().level.getGameTime() % 40 < 35 ? 0 : 240);
        }
    }

    private enum EnergyStatus {
        OK(null),
        NO_UPGRADES(Tooltip.create(xlate("modularrouters.itemText.misc.energyWarning.noBuffer").withStyle(ChatFormatting.GOLD))),
        ENERGY_LOW(Tooltip.create(xlate("modularrouters.itemText.misc.energyWarning").withStyle(ChatFormatting.GOLD)));

        private final Tooltip tooltip;

        EnergyStatus(Tooltip tooltip) {
            this.tooltip = tooltip;
        }

        public Tooltip getTooltip() {
            return tooltip;
        }
    }
}
