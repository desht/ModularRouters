package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.UniversalBucket;

import java.util.Arrays;
import java.util.List;

public class GuiModuleVacuum extends GuiModule {
    private XPTypeButton xpb;
    private EjectButton ejb;

    public GuiModuleVacuum(ContainerModule container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledVacuumModule vac = new CompiledVacuumModule(null, moduleItemStack);

        ItemStack[] icons = Arrays.stream(XPCollectionType.values()).map(XPCollectionType::getIcon).toArray(ItemStack[]::new);
        addButton(xpb = new XPTypeButton(guiLeft + 170, guiTop + 28, 16, 16, true, icons, vac.getXPCollectionType()));
        addButton(ejb = new EjectButton(guiLeft + 167, guiTop + 48, vac.isAutoEjecting()));

        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 24, guiLeft + 187, guiTop + 45, "guiText.popup.xpVacuum", guiContainer -> xpb.visible);
        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 46, guiLeft + 187, guiTop + 65, "guiText.popup.xpVacuum.eject", guiContainer -> xpb.visible);
    }

    @Override
    protected void setupButtonVisibility() {
        super.setupButtonVisibility();

        xpb.visible = augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT) > 0;
        ejb.visible = xpb.visible && !xpb.getState().isSolid();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);
        if (augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT) > 0) {
            font.drawString(I18n.format("guiText.label.xpVacuum"), 127, 32, 0xFFFFFF);
            if (!xpb.getState().isSolid()) {
                font.drawString(I18n.format("guiText.label.xpVacuum.eject"), 127, 52, 0xFFFFFF);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (augmentCounter.getAugmentCount(ModItems.XP_VACUUM_AUGMENT) > 0) {
            this.blit(guiLeft + 168, guiTop + 26, BUTTON_XY.x, BUTTON_XY.y, 18, 18);
        }
    }

    @Override
    protected CompoundNBT buildMessageData() {
        CompoundNBT compound = super.buildMessageData();
        compound.putInt(CompiledVacuumModule.NBT_XP_FLUID_TYPE, xpb.getState().ordinal());
        compound.putBoolean(CompiledVacuumModule.NBT_AUTO_EJECT, ejb.isToggled());
        return compound;
    }

    private class XPTypeButton extends ItemStackCyclerButton<XPCollectionType> {
        private final List<List<String>> tips = Lists.newArrayList();

        XPTypeButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, XPCollectionType initialVal) {
            super(x, y, width, height, flat, stacks, initialVal, GuiModuleVacuum.this);

            for (XPCollectionType type : XPCollectionType.values()) {
                String modName = ModNameCache.getModName(type.getModId());
                String title = type.getIcon().getItem() instanceof UniversalBucket ?
                        MiscUtil.getFluidName(type.getIcon()) : type.getIcon().getDisplayName().getString();
                tips.add(ImmutableList.of(title, TextFormatting.BLUE + "" + TextFormatting.ITALIC + modName));
            }
        }

        @Override
        public void setState(XPCollectionType newState) {
            super.setState(newState);

            ejb.visible = xpb.visible && !xpb.getState().isSolid();
        }

        @Override
        public List<String> getTooltip() {
            return tips.get(getState().ordinal());
        }

        @Override
        public boolean isApplicable(XPCollectionType state) {
            return state.isAvailable();
        }
    }

    private class EjectButton extends TexturedToggleButton {
        EjectButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, GuiModuleVacuum.this);
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 192 : 112;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }
}
