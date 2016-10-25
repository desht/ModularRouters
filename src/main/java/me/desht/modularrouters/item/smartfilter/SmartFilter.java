package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.FilterSlot;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.logic.RouterTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class SmartFilter {

    /**
     * Basic information for the module, which is always shown.
     */
    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiModule) {
            Slot slot = ((GuiModule) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
            if (slot instanceof FilterSlot) {
                list.add(MiscUtil.translate("itemText.misc.configureHint", String.valueOf(Config.configKey)));
            }
        }
    }

    /**
     * Usage information for the module, shown when Ctrl is held.
     */
    @SideOnly(Side.CLIENT)
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + itemstack.getItem().getUnlocalizedName(itemstack));
    }

    /**
     * Extra information for the module, shown when Shift is held.
     */
    @SideOnly(Side.CLIENT)
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        // nothing by default
    }

    /**
     * Create a run-time IItemMatcher object from the information in the item stacks.
     *
     * @param filterStack the filter item
     * @param moduleStack the module that the filter is in - may be null
     * @param target
     * @return a new IItemMatcher implementation
     */
    public abstract IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack, RouterTarget target);

    public abstract IRecipe getRecipe();

    public abstract Class<? extends GuiScreen> getGuiHandler();

    public boolean hasGuiContainer() {
        return false;
    }

    public Container createContainer(EntityPlayer player, ItemStack filterStack, TileEntityItemRouter router) {
        return null;
    }

    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float x, float y, float z) {
        return EnumActionResult.PASS;
    }

    public abstract IMessage dispatchMessage(FilterSettingsMessage message, ItemStack filterStack);

    public abstract int getSize(ItemStack filterStack);
}
