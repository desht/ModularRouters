package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public abstract class Upgrade {
    @SideOnly(Side.CLIENT)
    void addBasicInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag flag) {
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

    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }

    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
    }

    ShapelessOreRecipe makeShapelessOreRecipe(ItemStack result, Object... recipe) {
        return new ShapelessOreRecipe(new ResourceLocation(ModularRouters.MODID, "upgrade_recipe"), result, recipe);
    }

    ShapedOreRecipe makeShapedOreRecipe(ItemStack result, Object... recipe) {
        return new ShapedOreRecipe(new ResourceLocation(ModularRouters.MODID, "upgrade_recipe"), result, recipe);
    }
}
