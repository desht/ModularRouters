package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public class CamouflageUpgrade extends Upgrade {
    public static final String NBT_STATE_NAME = "BlockStateName";
    public static final String NBT_STATE_META = "BlockStateMeta";

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.CAMOUFLAGE),
                ModItems.blankUpgrade,
                new ItemStack(Items.DYE, 1, 1), new ItemStack(Items.DYE, 1, 2), new ItemStack(Items.DYE, 1, 4));
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey(NBT_STATE_NAME)) {
            list.add(I18n.format("itemText.camouflage.held", getCamoStateDisplayName(itemstack)));
        }
    }

    @Override
    boolean hasExtraInformation() {
        return true;
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        super.onCompiled(stack, router);
        router.setCamouflage(getCamoState(stack));
    }

    public static void setCamoState(ItemStack stack, IBlockState heldState) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();
        writeToNBT(compound, heldState);
    }

    public static void writeToNBT(NBTTagCompound compound, IBlockState heldState) {
        Block b = heldState.getBlock();
        compound.setString(NBT_STATE_NAME, b.getRegistryName().toString());
        compound.setInteger(NBT_STATE_META, b.getMetaFromState(heldState));
    }

    public static IBlockState readFromNBT(NBTTagCompound compound) {
        if (!compound.hasKey(NBT_STATE_NAME)) {
            return null;
        }
        Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(compound.getString(NBT_STATE_NAME)));
        return b != null ? b.getStateFromMeta(compound.getInteger(NBT_STATE_META)) : null;
    }

    public static IBlockState getCamoState(ItemStack stack) {
        return stack.hasTagCompound() ? readFromNBT(stack.getTagCompound()) : null;
    }

    public static String getCamoStateDisplayName(ItemStack stack) {
        IBlockState state = getCamoState(stack);
        if (state != null) {
            Block b = state.getBlock();
            Item item = Item.getItemFromBlock(b);
            if (item != null) {
                return new ItemStack(item, 1, b.getMetaFromState(state)).getDisplayName();
            }
        }
        return "<?>";
    }

    public static class Interacted {
        @SubscribeEvent
        public static void onInteracted(PlayerInteractEvent.RightClickBlock event) {
            EntityPlayer player = event.getEntityPlayer();
            ItemStack stack = player.getHeldItem(event.getHand());
            if (!player.getEntityWorld().isRemote && ItemUpgrade.isType(stack, ItemUpgrade.UpgradeType.CAMOUFLAGE) && player.isSneaking()) {
                IBlockState state = event.getWorld().getBlockState(event.getPos());
                if (isBlockOKForCamo(state, event.getWorld(), event.getPos())) {
                    setCamoState(stack, state);
                    event.getEntityPlayer().addChatMessage(new TextComponentTranslation("itemText.camouflage.held", getCamoStateDisplayName(stack)));
                }
            }
        }

        private static boolean isBlockOKForCamo(IBlockState state, World world, BlockPos pos) {
            if (state.getRenderType() != EnumBlockRenderType.MODEL)
                return false;

            AxisAlignedBB coll = state.getCollisionBoundingBox(world, pos);
            if (coll == null || coll.equals(Block.NULL_AABB))
                return false;

            return true;
        }
    }
}
