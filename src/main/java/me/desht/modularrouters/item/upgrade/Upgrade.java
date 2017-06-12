package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class Upgrade {
    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
    }

    boolean hasExtraInformation() {
        return false;
    }

    /**
     * Usage information for the upgrade, shown when Ctrl is held.
     */
    @SideOnly(Side.CLIENT)
    protected void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        MiscUtil.appendMultiline(list, "itemText.usage." + itemstack.getItem().getUnlocalizedName(itemstack), getExtraUsageParams());
    }

    public Object[] getExtraUsageParams() {
        return new Object[0];
    }

    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
    }

    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // no-op by default
    }

    public abstract IRecipe getRecipe();

    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }

    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
    }
}
