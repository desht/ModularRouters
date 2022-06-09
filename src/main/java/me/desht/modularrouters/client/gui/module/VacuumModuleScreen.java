package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class VacuumModuleScreen extends AbstractModuleScreen {
    private XPTypeButton xpb;
    private EjectButton ejb;

    public VacuumModuleScreen(ContainerModule container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledVacuumModule vac = new CompiledVacuumModule(null, moduleItemStack);

        ItemStack[] icons = Arrays.stream(XPCollectionType.values()).map(XPCollectionType::getIcon).toArray(ItemStack[]::new);
        addRenderableWidget(xpb = new XPTypeButton(leftPos + 170, topPos + 28, 16, 16, true, icons, vac.getXPCollectionType()));
        addRenderableWidget(ejb = new EjectButton(leftPos + 167, topPos + 48, vac.isAutoEjecting()));

        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 24, leftPos + 187, topPos + 45, "modularrouters.guiText.popup.xpVacuum", guiContainer -> xpb.visible);
        getMouseOverHelp().addHelpRegion(leftPos + 125, topPos + 46, leftPos + 187, topPos + 65, "modularrouters.guiText.popup.xpVacuum.eject", guiContainer -> xpb.visible);
    }

    @Override
    protected void setupButtonVisibility() {
        super.setupButtonVisibility();

        xpb.visible = augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT.get()) > 0;
        ejb.visible = xpb.visible && !xpb.getState().isSolid();
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        if (augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT.get()) > 0) {
            font.draw(matrixStack, xlate("modularrouters.guiText.label.xpVacuum"), 127, 32, 0xFFFFFF);
            if (!xpb.getState().isSolid()) {
                font.draw(matrixStack, xlate("modularrouters.guiText.label.xpVacuum.eject"), 127, 52, 0xFFFFFF);
            }
        }
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        if (augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT.get()) > 0) {
            this.blit(matrixStack,leftPos + 168, topPos + 26, BUTTON_XY.x(), BUTTON_XY.y(), 18, 18);
        }
    }

    @Override
    protected CompoundTag buildMessageData() {
        CompoundTag compound = super.buildMessageData();
        compound.putInt(CompiledVacuumModule.NBT_XP_FLUID_TYPE, xpb.getState().ordinal());
        compound.putBoolean(CompiledVacuumModule.NBT_AUTO_EJECT, ejb.isToggled());
        return compound;
    }

    private class XPTypeButton extends ItemStackCyclerButton<XPCollectionType> {
        private final List<List<Component>> tips = Lists.newArrayList();

        XPTypeButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, XPCollectionType initialVal) {
            super(x, y, width, height, flat, stacks, initialVal, VacuumModuleScreen.this);

            for (XPCollectionType type : XPCollectionType.values()) {
                MutableComponent modName = Component.literal(ModNameCache.getModName(type.getModId()));
                MutableComponent title = type.getDisplayName().plainCopy();
                tips.add(ImmutableList.of(title, modName.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
            }
        }

        @Override
        public void setState(XPCollectionType newState) {
            super.setState(newState);

            ejb.visible = xpb.visible && !xpb.getState().isSolid();
        }

        @Override
        public List<Component> getTooltip() {
            return tips.get(getState().ordinal());
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
