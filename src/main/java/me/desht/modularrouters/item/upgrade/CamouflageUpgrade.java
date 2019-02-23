package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ObjectRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class CamouflageUpgrade extends ItemUpgrade {
    public static final String NBT_STATE_NAME = "BlockStateName";

    public CamouflageUpgrade(Properties props) {
        super(props);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        if (itemstack.hasTag() && itemstack.getTag().contains(NBT_STATE_NAME)) {
            list.add(new TextComponentTranslation("itemText.camouflage.held", getCamoStateDisplayName(itemstack)));
        }
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        super.onCompiled(stack, router);
        router.setCamouflage(getCamoState(stack));
    }

    private static void setCamoState(ItemStack stack, IBlockState camoState) {
        stack.getOrCreateTag().put(NBT_STATE_NAME, NBTUtil.writeBlockState(camoState));
    }

    public static IBlockState readFromNBT(NBTTagCompound compound) {
        return NBTUtil.readBlockState(compound);
    }

    private static IBlockState getCamoState(ItemStack stack) {
        return stack.hasTag() ? readFromNBT(stack.getTag()) : null;
    }

    private static String getCamoStateDisplayName(ItemStack stack) {
        IBlockState state = getCamoState(stack);
        // TODO
        return "<CAMO TODO>";
//        if (state != null) {
//            state.
//            Block b = state.getBlock();
//            Item item = Item.getItemFromBlock(b);
//            if (item != null) {
//                return new ItemStack(item, 1, b.getMetaFromState(state)).getDisplayName();
//            }
//        }
//        return "<?>";
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext ctx) {
        EntityPlayer player = ctx.getPlayer();
        ItemStack stack = ctx.getItem();

        IBlockState state = ctx.getWorld().getBlockState(ctx.getPos());
        if (isBlockOKForCamo(state)) {
            setCamoState(stack, state);
            if (!ctx.getWorld().isRemote) {
                player.sendStatusMessage(new TextComponentTranslation("itemText.camouflage.held", getCamoStateDisplayName(stack)), false);
            } else {
                player.playSound(ObjectRegistry.SOUND_SUCCESS, 1.0f, 1.5f);
            }
            return EnumActionResult.SUCCESS;
        } else if (ctx.getWorld().isRemote) {
            player.playSound(ObjectRegistry.SOUND_ERROR, 1.0f, 1.0f);
            return EnumActionResult.FAIL;
        }
        return EnumActionResult.PASS;
    }

    private static boolean isBlockOKForCamo(IBlockState state) {
        // trying to camo a router as itself = recursion hell
        return state.getRenderType() == EnumBlockRenderType.MODEL && state.getBlock() != ObjectRegistry.ITEM_ROUTER;
    }
}
