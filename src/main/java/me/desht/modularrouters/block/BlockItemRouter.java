package me.desht.modularrouters.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.integration.TOPInfoProvider;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class BlockItemRouter extends BlockBase implements ITileEntityProvider, TOPInfoProvider {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockItemRouter() {
        super(Material.IRON, "itemRouter");
        setHardness(5.0f);
        setDefaultState(getDefaultState().withProperty(ACTIVE, false));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityItemRouter) {
            return state.withProperty(ACTIVE, ((TileEntityItemRouter) te).isActive());
        } else {
            return state;
        }
    }

    @Override
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (blockFaceClickedOn.getFrontOffsetY() == 0) {
            // placed against a vertical surface - have it face away from that surface
            return this.getDefaultState().withProperty(FACING, blockFaceClickedOn);
        } else {
            // have the router face the player
            EnumFacing enumfacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
            return this.getDefaultState().withProperty(FACING, enumfacing.getOpposite());
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        TileEntityItemRouter te = (TileEntityItemRouter) world.getTileEntity(pos);
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            ((ItemStackHandler) te.getModules()).deserializeNBT(compound.getCompoundTag("Modules"));
            ((ItemStackHandler) te.getUpgrades()).deserializeNBT(compound.getCompoundTag("Upgrades"));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getHorizontal(meta);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getHorizontalIndex();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTIVE);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityItemRouter();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState blockstate) {
        TileEntityItemRouter te = (TileEntityItemRouter) world.getTileEntity(pos);
        if (te != null) {
            InventoryUtils.dropInventoryItems(world, pos, te.getBuffer());
            world.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(world, pos, blockstate);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        TileEntityItemRouter te = (TileEntityItemRouter) world.getTileEntity(pos);
        if (te != null) {
            ItemStack stack = te.getBufferItemStack();
            return stack == null ? 0 : MathHelper.floor_float(1 + ((float) stack.stackSize / (float) stack.getMaxStackSize()) * 14);
        } else {
            return 0;
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        // If it will harvest, delay deletion of the block until after getDrops
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> l = new ArrayList<>();

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityItemRouter) {
            TileEntityItemRouter router = (TileEntityItemRouter) te;
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            if (router.getModuleCount() > 0 || router.getSpeedUpgrades() > 0 || router.getStackUpgrades() > 0) {
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }
                NBTTagCompound compound = stack.getTagCompound();
                compound.setTag("Modules", ((ItemStackHandler) router.getModules()).serializeNBT());
                compound.setTag("Upgrades", ((ItemStackHandler) router.getUpgrades()).serializeNBT());
                compound.setInteger("ModuleCount", router.getModuleCount());
                compound.setInteger("StackUpgradeCount", router.getStackUpgrades());
                compound.setInteger("SpeedUpgradeCount", router.getSpeedUpgrades());
            }
            l.add(stack);
        }
        return l;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        NBTTagCompound compound = itemstack.getTagCompound();
        if (compound != null) {
            if (GuiScreen.isShiftKeyDown()) {
                list.add(I18n.format("itemText.misc.moduleCount", compound.getInteger("ModuleCount")));
                list.add(compound.getInteger("StackUpgradeCount") + " x " + I18n.format("item.stackUpgrade.name"));
                list.add(compound.getInteger("SpeedUpgradeCount") + " x " + I18n.format("item.speedUpgrade.name"));
            } else {
                list.add(I18n.format("itemText.misc.holdShift"));
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && !player.isSneaking()) {
            player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
    }

    private static char[] ARROWS = new char[]{' ', '▼', '▲', '◀', '▶', '▣', '▤'};

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof TileEntityItemRouter) {
            TileEntityItemRouter router = (TileEntityItemRouter) te;
            IItemHandler modules = router.getModules();
            IProbeInfo sub = probeInfo.horizontal();
            for (int i = 0; i < modules.getSlots(); i++) {
                ItemStack stack = modules.getStackInSlot(i);
                if (stack != null) {
                    Module.RelativeDirection dir = Module.getDirectionFromNBT(stack);
                    sub.item(stack).text(TextFormatting.GREEN + Character.toString(ARROWS[dir.ordinal()]));
                }
            }
            sub = probeInfo.horizontal();
            for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                sub.item(ItemUpgrade.makeItemStack(type)).text(TextFormatting.GREEN + "x " + router.getUpgradeCount(type));
            }
            probeInfo.text(TextFormatting.RED + net.minecraft.util.text.translation.I18n.translateToLocal("guiText.tooltip.redstone." + router.getRedstoneBehaviour()));
        }
    }
}
