package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule.VacuumSettings;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class VacuumModuleScreen extends ModuleScreen {
    private XPTypeButton xpb;
    private EjectButton ejb;

    public VacuumModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        VacuumSettings settings = moduleItemStack.getOrDefault(ModDataComponents.VACUUM_SETTINGS, VacuumSettings.DEFAULT);

        ItemStack[] icons = Arrays.stream(XPCollectionType.values()).map(XPCollectionType::getIcon).toArray(ItemStack[]::new);
        addRenderableWidget(xpb = new XPTypeButton(leftPos + 127, topPos + 30, 20, 20, false, icons, settings.collectionType()));
        addRenderableWidget(ejb = new EjectButton(leftPos + 127, topPos + 64, settings.autoEject()));

        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 16, leftPos + 187, topPos + 52,
                "modularrouters.guiText.popup.xpVacuum", guiContainer -> xpb.visible);
        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 52, leftPos + 187, topPos + 88,
                "modularrouters.guiText.popup.xpVacuum.eject", guiContainer -> xpb.visible && ejb.visible);
    }

    @Override
    protected void setupButtonVisibility() {
        super.setupButtonVisibility();

        xpb.visible = augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT.get()) > 0;
        ejb.visible = xpb.visible && !xpb.getState().isSolid();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT.get()) > 0) {
            graphics.text(font, xlate("modularrouters.guiText.label.xpVacuum"), 127, 18, 0xFFFFFF);
            if (!xpb.getState().isSolid()) {
                graphics.text(font, xlate("modularrouters.guiText.label.xpVacuum.eject"), 127, 52, 0xFFFFFF);
            }
        }
    }

    @Override
    protected void buildComponentPatch(DataComponentPatch.Builder builder) {
        super.buildComponentPatch(builder);
        builder.set(ModDataComponents.VACUUM_SETTINGS.get(), new VacuumSettings(ejb.isToggled(), xpb.getState()));
    }

    private class XPTypeButton extends ItemStackCyclerButton<XPCollectionType> {
        XPTypeButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, XPCollectionType initialVal) {
            super(x, y, width, height, flat, stacks, initialVal, VacuumModuleScreen.this);
        }

        @Override
        protected Tooltip makeTooltip(XPCollectionType type) {
            return Tooltip.create(type.getDisplayName().copy().append("\n")
                    .append(Component.literal(ModNameCache.getModName(type.getModId()))
                            .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
            );
        }

        @Override
        public void setState(XPCollectionType newState) {
            super.setState(newState);

            if (ejb != null) {
                ejb.visible = this.visible && !xpb.getState().isSolid();
            }
        }

        @Override
        public boolean isApplicable(XPCollectionType state) {
            return state.isAvailable();
        }
    }

    private class EjectButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(112, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(192, 16);

        EjectButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, VacuumModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }
}
