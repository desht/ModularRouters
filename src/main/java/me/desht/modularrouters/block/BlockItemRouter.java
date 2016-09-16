package me.desht.modularrouters.block;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.integration.top.ElementModule;
import me.desht.modularrouters.integration.top.TOPInfoProvider;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class BlockItemRouter extends BlockBase implements ITileEntityProvider, TOPInfoProvider {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    public static final PropertyBool OPEN_F = PropertyBool.create("open_f");
    public static final PropertyBool OPEN_B = PropertyBool.create("open_b");
    public static final PropertyBool OPEN_U = PropertyBool.create("open_u");
    public static final PropertyBool OPEN_D = PropertyBool.create("open_d");
    public static final PropertyBool OPEN_L = PropertyBool.create("open_l");
    public static final PropertyBool OPEN_R = PropertyBool.create("open_r");

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
            TileEntityItemRouter router = (TileEntityItemRouter) te;
            state = state.withProperty(ACTIVE, router.isActive());
            for (Module.RelativeDirection side : Module.RelativeDirection.realSides()) {
                state = state.withProperty(side.getProperty(), router.isSideOpen(side));
            }
        }
        return state;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
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
            try {
                te.setRedstoneBehaviour(RouterRedstoneBehaviour.valueOf(compound.getString("RedstoneBehaviour")));
            } catch (IllegalArgumentException e) {
                te.setRedstoneBehaviour(RouterRedstoneBehaviour.ALWAYS);
            }
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
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        IBlockState state = world.getBlockState(pos);
        if (axis.getAxis() != EnumFacing.Axis.Y) {
            world.setBlockState(pos, state.withProperty(FACING, axis));
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityItemRouter) {
                ((TileEntityItemRouter) te).recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
            }
            return true;
        }
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTIVE, OPEN_F, OPEN_B, OPEN_U, OPEN_D, OPEN_L, OPEN_R);
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
                compound.setString("RedstoneBehaviour", router.getRedstoneBehaviour().toString());
                compound.setInteger("ModuleCount", router.getModuleCount());
                for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                    compound.setInteger("UpgradeCount." + type, router.getUpgradeCount(type));
                }
            }
            l.add(stack);
        }
        return l;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        NBTTagCompound compound = itemstack.getTagCompound();
        if (compound != null && compound.hasKey("ModuleCount")) {
            list.add(I18n.format("itemText.misc.routerConfigured"));
            MiscUtil.appendMultiline(list, "itemText.misc.moduleCount", compound.getInteger("ModuleCount"));
            for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                int c = compound.getInteger("UpgradeCount." + type);
                if (c > 0) {
                    String name = I18n.format("item." + type.toString().toLowerCase() + "Upgrade.name");
                    list.add(I18n.format("itemText.misc.upgradeCount", name, c));
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && !player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityItemRouter) {
                // TODO allow op override
                if (((TileEntityItemRouter) te).isPermitted(player)) {
                    player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, world, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    player.addChatMessage(new TextComponentTranslation("chatText.security.accessDenied"));
                }
            }
        }
        return true;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof TileEntityItemRouter) {
            TileEntityItemRouter router = (TileEntityItemRouter) te;
            if (router.isPermitted(player)) {
                IItemHandler modules = router.getModules();
                IProbeInfo sub = probeInfo.horizontal();
                for (int i = 0; i < modules.getSlots(); i++) {
                    ItemStack stack = modules.getStackInSlot(i);
                    if (stack != null) {
                        sub.element(new ElementModule(stack));
                    }
                }
                sub = probeInfo.horizontal();
                for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                    if (router.getUpgradeCount(type) > 0) {
                        sub.item(ItemUpgrade.makeItemStack(type, router.getUpgradeCount(type)));
                    }
                }
                probeInfo.text(MiscUtil.translate("guiText.tooltip.redstone." + router.getRedstoneBehaviour()));
            } else {
                probeInfo.text(MiscUtil.translate("chatText.security.accessDenied"));
            }
        }
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityItemRouter) {
            return ((TileEntityItemRouter) te).getRedstoneLevel(side, false);
        } else {
            return 0;
        }
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof TileEntityItemRouter) {
            return ((TileEntityItemRouter) te).getRedstoneLevel(side, true);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        // ensure levers etc. can be attached to the block even though it can emit redstone
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }
}
