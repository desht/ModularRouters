package me.desht.modularrouters.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.client.gui.widgets.WidgetEnergy;
import me.desht.modularrouters.client.gui.widgets.button.RedstoneBehaviourButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.RouterSettingsMessage;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.theClientWorld;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.RL;

public class GuiItemRouter extends GuiContainerBase<ContainerItemRouter> implements ISendToServer, IHasContainer<ContainerItemRouter> {
    private static final ResourceLocation TEXTURE_LOCATION = RL("textures/gui/router.png");

    private static final int LABEL_YPOS = 5;
    private static final int MODULE_LABEL_YPOS = 60;
    private static final int BUFFER_LABEL_YPOS = 28;
    private static final int UPGRADES_LABEL_YPOS = 28;
    private static final int GUI_HEIGHT = 186;
    private static final int GUI_WIDTH = 176;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 16;

    private static final int MODULE_START = ContainerItemRouter.TE_FIRST_SLOT + ContainerItemRouter.MODULE_SLOT_START;
    private static final int MODULE_END = ContainerItemRouter.TE_FIRST_SLOT + ContainerItemRouter.MODULE_SLOT_END;

    private RedstoneBehaviourButton redstoneBehaviourButton;
    private EcoButton ecoButton;
    private EnergyDirectionButton energyDirButton;
    private WidgetEnergy energyWidget;
    private EnergyWarningButton energyWarning;
    private int energyUsage;

    public GuiItemRouter(ContainerItemRouter container, PlayerInventory inventoryPlayer, ITextComponent displayName) {
        super(container, inventoryPlayer, displayName);

        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;

        this.passEvents = true;
    }

    @Override
    public void init() {
        super.init();

        TileEntityItemRouter router = menu.getRouter();

        addButton(redstoneBehaviourButton = new RedstoneBehaviourButton(this.leftPos + 152, this.topPos + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getRedstoneBehaviour(), this));
        addButton(ecoButton = new EcoButton(this.leftPos + 132, this.topPos + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getEcoMode()));
        addButton(energyDirButton = new EnergyDirectionButton(this.leftPos - 8, this.topPos + 40, router.getEnergyDirection()));
        addButton(energyWidget = new WidgetEnergy(this.leftPos - 22, this.topPos + 15, router.getEnergyStorage()));
        addButton(energyWarning = new EnergyWarningButton(this.leftPos + 4, this.topPos + 4));
        energyWidget.visible = energyDirButton.visible = router.getEnergyCapacity() > 0;
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        String title = I18n.get("block.modularrouters.item_router");
        font.draw(matrixStack, title, this.imageWidth / 2f - font.width(title) / 2f, LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, I18n.get("modularrouters.guiText.label.buffer"), 8, BUFFER_LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, I18n.get("modularrouters.guiText.label.upgrades"), ContainerItemRouter.UPGRADE_XPOS, UPGRADES_LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, I18n.get("modularrouters.guiText.label.modules"), ContainerItemRouter.MODULE_XPOS, MODULE_LABEL_YPOS, 0xFF404040);
        font.draw(matrixStack, I18n.get("container.inventory"), 8, this.imageHeight - 96 + 4, 0xFF404040);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float v, int i, int i1) {
        getMinecraft().getTextureManager().bind(TEXTURE_LOCATION);
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
    public void tick() {
        super.tick();

        energyUsage = 0;
        for (int i = MODULE_START; i < MODULE_END; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.getItem() instanceof ItemModule) {
                energyUsage += ((ItemModule) stack.getItem()).getEnergyCost();
            }
        }

        boolean hasEnergyUpgrade = menu.getRouter().getEnergyCapacity() > 0;
        energyWidget.visible = hasEnergyUpgrade;
        energyDirButton.visible = hasEnergyUpgrade
                && getMenu().getSlot(ContainerItemRouter.TE_FIRST_SLOT).getItem().getCapability(CapabilityEnergy.ENERGY).isPresent();

        energyWarning.x = hasEnergyUpgrade ? leftPos - 22 : leftPos + 4;

    }

    private boolean handleModuleConfig() {
        Slot slot = getSlotUnderMouse();
        if (slot == null || !(slot.getItem().getItem() instanceof ItemModule) || slot.index < MODULE_START || slot.index > MODULE_END) {
            return false;
        }
        MFLocator locator = MFLocator.moduleInRouter(menu.getRouter().getBlockPos(), slot.index - MODULE_START);
        PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
        return true;
    }

    @Override
    public void sendToServer() {
        TileEntityItemRouter router = menu.getRouter();

        router.setRedstoneBehaviour(redstoneBehaviourButton.getState());
        router.setEcoMode(ecoButton.isToggled());
        router.setEnergyDirection(energyDirButton.getState());
        PacketHandler.NETWORK.sendToServer(new RouterSettingsMessage(router));
    }

    public Collection<Rectangle2d> getExtraArea() {
        // for JEI's benefit
        return menu.getRouter().getEnergyCapacity() > 0 ?
                Collections.singletonList(new Rectangle2d(leftPos - 27, topPos, 32, 100)) :
                Collections.emptyList();
    }

    private class EcoButton extends TexturedToggleButton {
        EcoButton(int x, int y, int width, int height, boolean initialVal) {
            super(x, y, width, height, initialVal, GuiItemRouter.this);
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 96 : 80;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }

        @Override
        public List<ITextComponent> getTooltip() {
            return MiscUtil.wrapStringAsTextComponent(
                    I18n.get("modularrouters.guiText.tooltip.eco." + isToggled(),
                            MRConfig.Common.Router.ecoTimeout / 20.f,
                            MRConfig.Common.Router.lowPowerTickRate / 20.f)
            );
        }
    }

    private class EnergyDirectionButton extends TexturedCyclerButton<TileEntityItemRouter.EnergyDirection> {
        public EnergyDirectionButton(int x, int y, TileEntityItemRouter.EnergyDirection initialVal) {
            super(x, y, 14, 14, initialVal, GuiItemRouter.this);
        }

        @Override
        protected int getTextureX() {
            switch (getState()) {
                case TO_ROUTER: return 224;
                case FROM_ROUTER: return 144;
                case NONE: return 176;
            }
            throw new IllegalStateException();
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
        public List<ITextComponent> getTooltip() {
            return ImmutableList.of(
                    xlate(getState().getTranslationKey()),
                    xlate("modularrouters.guiText.tooltip.energy.rate",
                            MiscUtil.commify(menu.getRouter().getEnergyXferRate()))
                            .withStyle(TextFormatting.GRAY)
            );
        }
    }

    private class EnergyWarningButton extends TexturedButton {
        public EnergyWarningButton(int x, int y) {
            super(x, y, 16, 16, b -> {});
        }

        @Override
        public List<ITextComponent> getTooltip() {
            if (energyUsage < menu.getRouter().getEnergyStorage().getEnergyStored()) return Collections.emptyList();
            return menu.getRouter().getEnergyCapacity() > 0 ?
                    GuiUtil.xlateAndSplit("modularrouters.itemText.misc.energyWarning") :
                    GuiUtil.xlateAndSplit("modularrouters.itemText.misc.energyWarning.noBuffer");
        }

        @Override
        public void playDownSound(SoundHandler p_230988_1_) {
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        protected int getTextureX() {
            return 240;
        }

        @Override
        protected int getTextureY() {
            return menu.getRouter().getEnergyStorage().getEnergyStored() < energyUsage && theClientWorld().getGameTime() % 40 < 35 ? 0 : 240;
        }
    }
}
