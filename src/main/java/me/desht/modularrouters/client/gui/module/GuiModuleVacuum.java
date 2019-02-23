package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.integration.XPCollection.XPCollectionType;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.UniversalBucket;

import java.util.Arrays;
import java.util.List;

public class GuiModuleVacuum extends GuiModule {
    private static final int XP_TYPE_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;
    private static final int EJECT_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 1;

    private XPTypeButton xpb;
    private EjectButton ejb;
    private XPCollectionType xpCollectionType;
    private boolean autoEject;

    public GuiModuleVacuum(ContainerModule container) {
        super(container);

        CompiledVacuumModule vac = new CompiledVacuumModule(null, moduleItemStack);
        xpCollectionType = vac.getXPCollectionType();
        autoEject = vac.isAutoEjecting();
    }

    @Override
    public void initGui() {
        super.initGui();

        ItemStack[] xpTypeIcons = Arrays.stream(XPCollectionType.values()).map(XPCollectionType::getIcon).toArray(ItemStack[]::new);
        xpb = new XPTypeButton(XP_TYPE_BUTTON_ID, guiLeft + 170, guiTop + 28, 16, 16, true, xpTypeIcons, xpCollectionType) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                XPCollectionType oldType = getState();
                xpCollectionType = cycle(!GuiScreen.isShiftKeyDown());
                if (xpCollectionType != oldType) {
                    sendModuleSettingsToServer();
                }
            }
        };
        addButton(xpb);

        ejb = new EjectButton(EJECT_BUTTON_ID, guiLeft + 167, guiTop + 48, autoEject) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                toggle();
                autoEject = isToggled();
                sendModuleSettingsToServer();
            }
        };
        addButton(ejb);

        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 24, guiLeft + 187, guiTop + 45, "guiText.popup.xpVacuum", guiContainer -> xpb.visible);
        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 46, guiLeft + 187, guiTop + 65, "guiText.popup.xpVacuum.eject", guiContainer -> xpb.visible);
    }

    @Override
    protected void setupButtonVisibility() {
        super.setupButtonVisibility();

        xpb.visible = augmentCounter.getAugmentCount(ObjectRegistry.XP_VACUUM_AUGMENT) > 0;
        ejb.visible = xpb.visible && !xpb.getState().isSolid();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);
        if (augmentCounter.getAugmentCount(ObjectRegistry.XP_VACUUM_AUGMENT) > 0) {
            fontRenderer.drawString(I18n.format("guiText.label.xpVacuum"), 127, 32, 0xFFFFFF);
            if (!xpb.getState().isSolid()) {
                fontRenderer.drawString(I18n.format("guiText.label.xpVacuum.eject"), 127, 52, 0xFFFFFF);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (augmentCounter.getAugmentCount(ObjectRegistry.XP_VACUUM_AUGMENT) > 0) {
            this.drawTexturedModalRect(guiLeft + 168, guiTop + 26, BUTTON_XY.x, BUTTON_XY.y, 18, 18);
        }
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.putInt(CompiledVacuumModule.NBT_XP_FLUID_TYPE, xpCollectionType.ordinal());
        compound.putBoolean(CompiledVacuumModule.NBT_AUTO_EJECT, autoEject);
        return compound;
    }

    private class XPTypeButton extends ItemStackCyclerButton<XPCollectionType> {
        private final List<List<String>> tips = Lists.newArrayList();

        XPTypeButton(int buttonId, int x, int y, int width, int height, boolean flat, ItemStack[] stacks, XPCollectionType initialVal) {
            super(buttonId, x, y, width, height, flat, stacks, initialVal);

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
        EjectButton(int buttonId, int x, int y, boolean initialVal) {
            super(buttonId, x, y, 16, 16, initialVal);
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
