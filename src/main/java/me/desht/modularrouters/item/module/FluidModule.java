package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleFluid;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.FluidMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;

public class FluidModule extends Module {
    public enum FluidDirection {
        IN,  // to router
        OUT  // from router
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledFluidModule(router, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(itemstack, player, list, par4);
        CompiledFluidModule cfm = new CompiledFluidModule(null, itemstack);
        String dir = I18n.format("itemText.fluid.direction." + cfm.getFluidDirection());
        list.add(I18n.format("itemText.fluid.direction", dir));
        list.add(I18n.format("itemText.fluid.maxTransfer", cfm.getMaxTransfer()));
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.FLUID),
                " c ", "gmg",
                'm', ModItems.blankModule, 'c', Items.CAULDRON, 'g', Blocks.GLASS);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleFluid.class;
    }

    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        // only fluid-holding items or a smart filter item can go into a fluid module's filter
        return stack == null
                || (stack.stackSize == 1 && FluidUtil.getFluidContained(stack) != null)
                || ItemSmartFilter.getFilter(stack) != null;
    }

    @Override
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new FluidMatcher(stack);
    }

    @Override
    public boolean isFluidModule() {
        return true;
    }
}
