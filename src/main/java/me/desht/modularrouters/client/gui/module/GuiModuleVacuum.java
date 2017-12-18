package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.integration.XPFluids;
import me.desht.modularrouters.integration.XPFluids.XPCollectionType;
import me.desht.modularrouters.item.augment.ItemAugment.AugmentType;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.List;

public class GuiModuleVacuum extends GuiModule {
    private static final int XP_TYPE_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;

    private XPTypeButton xpb;
    private XPCollectionType xpCollectionType;

    public GuiModuleVacuum(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleVacuum(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledVacuumModule vac = new CompiledVacuumModule(null, moduleItemStack);
        xpCollectionType = vac.getXPCollectionType();
    }

    @Override
    public void initGui() {
        ItemStack[] stacks = new ItemStack[XPFluids.AVAILABLE_XP_COLLECTION_TYPES.size()];
        stacks[0] = new ItemStack(Items.EXPERIENCE_BOTTLE);
        for (int i = 1; i < XPFluids.AVAILABLE_XP_COLLECTION_TYPES.size(); i++) {
            Fluid fluid = FluidRegistry.getFluid(XPFluids.AVAILABLE_XP_COLLECTION_TYPES.get(i).getRegistryName());
            if (fluid != null) {
                stacks[i] = FluidUtil.getFilledBucket(new FluidStack(fluid, 1000));
            } else {
                stacks[i] = new ItemStack(Items.BUCKET);
            }
        }
        xpb = new XPTypeButton(XP_TYPE_BUTTON_ID, guiLeft + 170, guiTop + 28, 16, 16, true, stacks, xpCollectionType);

        super.initGui();
        xpb.x = guiLeft + 170; xpb.y = guiTop + 28;

        buttonList.add(xpb);

        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 24, guiLeft + 187, guiTop + 45, "guiText.popup.xpVacuum", guiContainer -> xpb.visible);
    }

    @Override
    protected void setupButtonVisibility() {
        super.setupButtonVisibility();

        xpb.visible = augmentCounter.getAugmentCount(AugmentType.XP_VACUUM) > 0;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);
        if (augmentCounter.getAugmentCount(AugmentType.XP_VACUUM) > 0) {
            fontRenderer.drawString(I18n.format("guiText.label.xpVacuum"), 127, 32, 0xFFFFFF);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        if (augmentCounter.getAugmentCount(AugmentType.XP_VACUUM) > 0) {
            this.drawTexturedModalRect(guiLeft + 168, guiTop + 26, BUTTON_XY.x, BUTTON_XY.y, 18, 18);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == XP_TYPE_BUTTON_ID) {
            XPTypeButton xpb = (XPTypeButton) button;
            XPCollectionType oldType = xpb.getState();
            xpCollectionType = xpb.cycle(!GuiScreen.isShiftKeyDown());
            if (xpCollectionType != oldType) {
                sendModuleSettingsToServer();
            }
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.setInteger(CompiledVacuumModule.NBT_XP_FLUID_TYPE, xpCollectionType.ordinal());
        return compound;
    }

    private static class XPTypeButton extends ItemStackCyclerButton<XPCollectionType> {
        private final List<List<String>> tips = Lists.newArrayList();

        public XPTypeButton(int buttonId, int x, int y, int width, int height, boolean flat, ItemStack[] stacks, XPCollectionType initialVal) {
            super(buttonId, x, y, width, height, flat, stacks, initialVal);

            for (XPCollectionType type : XPCollectionType.values()) {
                String modName = ModNameCache.getModName(type.getModId());
                tips.add(ImmutableList.of(
                        I18n.format("guiText.label.xpVacuum." + type),
                        TextFormatting.BLUE + "" + TextFormatting.ITALIC + modName)
                );
            }
        }

        @Override
        public List<String> getTooltip() {
            return tips.get(getState().ordinal());
        }
    }
}
