package me.desht.modularrouters.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.gui.widgets.WidgetEnergy;
import me.desht.modularrouters.client.gui.widgets.button.RedstoneBehaviourButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.RouterMenu;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.RouterSettingsMessage;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Collections;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.theClientWorld;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModularRouterScreen extends AbstractMRContainerScreen<RouterMenu> implements ISendToServer, MenuAccess<RouterMenu> {
    private static final ResourceLocation TEXTURE_LOCATION = RL("textures/gui/router.png");

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
    private WidgetEnergy energyWidget;
    private EnergyWarningButton energyWarning;
    private int energyUsage;

    public ModularRouterScreen(RouterMenu container, Inventory inventoryPlayer, Component displayName) {
        super(container, inventoryPlayer, displayName);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        this.passEvents = true;
    }

    @Override
    public void init() {
        super.init();

        ModularRouterBlockEntity router = menu.getRouter();

        addRenderableWidget(redstoneBehaviourButton = new RedstoneBehaviourButton(this.leftPos + 152, this.topPos + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getRedstoneBehaviour(), this));
        addRenderableWidget(ecoButton = new EcoButton(this.leftPos + 132, this.topPos + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getEcoMode()));
        addRenderableWidget(energyDirButton = new EnergyDirectionButton(this.leftPos - 8, this.topPos + 40, router.getEnergyDirection()));
        addRenderableWidget(energyWidget = new WidgetEnergy(this.leftPos - 22, this.topPos + 15, router.getEnergyStorage()));
        addRenderableWidget(energyWarning = new EnergyWarningButton(this.leftPos + 4, this.topPos + 4));
        energyWidget.visible = energyDirButton.visible = router.getEnergyCapacity() > 0;
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        MutableComponent title = xlate("block.modularrouters.modular_router");
        font.draw(matrixStack, title, this.imageWidth / 2f - font.width(title) / 2f, LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, xlate("modularrouters.guiText.label.buffer"), 8, BUFFER_LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, xlate("modularrouters.guiText.label.upgrades"), RouterMenu.UPGRADE_XPOS, UPGRADES_LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, xlate("modularrouters.guiText.label.modules"), RouterMenu.MODULE_XPOS, MODULE_LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, xlate("container.inventory"), 8, this.imageHeight - 96 + 4, 0xFF404040);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float v, int i, int i1) {
        GuiUtil.bindTexture(TEXTURE_LOCATION);
        blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.getRouter().getEnergyCapacity() > 0) {
            blit(matrixStack, leftPos - 27, topPos, 180, 0, 32, 100);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return ClientSetup.keybindConfigure.getKey().getValue() == keyCode ? handleModuleConfig() : super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        return btn == 2 ? handleModuleConfig() : super.mouseClicked(x, y, btn);
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
        energyDirButton.visible = hasEnergyUpgrade
                && getMenu().getSlot(RouterMenu.TE_FIRST_SLOT).getItem().getCapability(ForgeCapabilities.ENERGY).isPresent();

        energyWarning.setX(hasEnergyUpgrade ? leftPos - 22 : leftPos + 4);

    }

    private boolean handleModuleConfig() {
        Slot slot = getSlotUnderMouse();
        if (slot == null || !(slot.getItem().getItem() instanceof ModuleItem) || slot.index < MODULE_START || slot.index > MODULE_END) {
            return false;
        }
        MFLocator locator = MFLocator.moduleInRouter(menu.getRouter().getBlockPos(), slot.index - MODULE_START);
        PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
        return true;
    }

    @Override
    public void sendToServer() {
        ModularRouterBlockEntity router = menu.getRouter();

        router.setRedstoneBehaviour(redstoneBehaviourButton.getState());
        router.setEcoMode(ecoButton.isToggled());
        router.setEnergyDirection(energyDirButton.getState());
        PacketHandler.NETWORK.sendToServer(new RouterSettingsMessage(router));
    }

    public List<Rect2i> getExtraArea() {
        // for JEI's benefit
        return menu.getRouter().getEnergyCapacity() > 0 ?
                Collections.singletonList(new Rect2i(leftPos - 27, topPos, 32, 100)) :
                Collections.emptyList();
    }

    private class EcoButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(80, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(96, 16);

        EcoButton(int x, int y, int width, int height, boolean initialVal) {
            super(x, y, width, height, initialVal, ModularRouterScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }

        @Override
        public List<Component> getTooltip() {
            return GuiUtil.xlateAndSplit("modularrouters.guiText.tooltip.eco." + isToggled(),
                    ConfigHolder.common.router.ecoTimeout.get() / 20.f,
                    ConfigHolder.common.router.lowPowerTickRate.get() / 20.f);
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

        @Override
        public List<Component> getTooltip() {
            return ImmutableList.of(
                    xlate(getState().getTranslationKey()),
                    xlate("modularrouters.guiText.tooltip.energy.rate",
                            MiscUtil.commify(menu.getRouter().getEnergyXferRate()))
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private class EnergyWarningButton extends TexturedButton {
        public EnergyWarningButton(int x, int y) {
            super(x, y, 16, 16, b -> {});
        }

        @Override
        public List<Component> getTooltip() {
            if (energyUsage <= menu.getRouter().getEnergyStorage().getEnergyStored()) return Collections.emptyList();
            return menu.getRouter().getEnergyCapacity() > 0 ?
                    GuiUtil.xlateAndSplit("modularrouters.itemText.misc.energyWarning") :
                    GuiUtil.xlateAndSplit("modularrouters.itemText.misc.energyWarning.noBuffer");
        }

        @Override
        public void playDownSound(SoundManager p_230988_1_) {
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        protected XYPoint getTextureXY() {
            boolean lowEnergy = menu.getRouter().getEnergyStorage().getEnergyStored() < energyUsage;
            return new XYPoint(240, lowEnergy && theClientWorld().getGameTime() % 40 < 35 ? 0 : 240);
        }
    }
}
