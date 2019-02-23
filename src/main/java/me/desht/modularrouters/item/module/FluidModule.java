package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModuleFluid;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidUtil;

import java.awt.*;
import java.util.List;

public class FluidModule extends ItemModule {
    public FluidModule(Properties props) {
        super(props);
    }

    public enum FluidDirection {
        IN,  // to router
        OUT  // from router
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledFluidModule(router, stack);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
        super.addExtraInformation(stack, list);
        CompiledFluidModule cfm = new CompiledFluidModule(null, stack);
        String dir = I18n.format("itemText.fluid.direction." + cfm.getFluidDirection());
        list.add(new TextComponentTranslation("itemText.fluid.direction", dir));
        list.add(new TextComponentTranslation("itemText.fluid.maxTransfer", cfm.getMaxTransfer()));
    }

    @Override
    public Class<? extends GuiModule> getGuiClass() {
        return GuiModuleFluid.class;
    }

    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        // only fluid-holding items or a smart filter item can go into a fluid module's filter
        return stack.isEmpty()
                || (stack.getCount() == 1 && FluidUtil.getFluidContained(stack).isPresent())
                || stack.getItem() instanceof ItemSmartFilter;
    }

    @Override
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new FluidMatcher(stack);
    }

    @Override
    public boolean isFluidModule() {
        return true;
    }

    @Override
    public Color getItemTint() {
        return new Color(79, 191, 255);
    }
}
