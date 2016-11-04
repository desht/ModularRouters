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
import me.desht.modularrouters.sound.MRSoundEvents;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.PropertyObject;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockItemRouter extends BlockBase implements ITileEntityProvider, TOPInfoProvider {
    public static final String BLOCK_NAME = "itemRouter";

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool ACTIVE = PropertyBool.create("active");
    public static final PropertyBool OPEN_F = PropertyBool.create("open_f");
    public static final PropertyBool OPEN_B = PropertyBool.create("open_b");
    public static final PropertyBool OPEN_U = PropertyBool.create("open_u");
    public static final PropertyBool OPEN_D = PropertyBool.create("open_d");
    public static final PropertyBool OPEN_L = PropertyBool.create("open_l");
    public static final PropertyBool OPEN_R = PropertyBool.create("open_r");
    public static final PropertyBool CAN_EMIT = PropertyBool.create("can_emit");
    public static final PropertyObject<IBlockState> CAMOUFLAGE_STATE = new PropertyObject<>("held_state", IBlockState.class);

    public static final String NBT_MODULES = "Modules";
    public static final String NBT_UPGRADES = "Upgrades";
    public static final String NBT_MODULE_COUNT = "ModuleCount";
    public static final String NBT_UPGRADE_COUNT = "UpgradeCount";
    public static final String NBT_REDSTONE_BEHAVIOUR = "RedstoneBehaviour";

    public BlockItemRouter() {
        super(Material.IRON, BLOCK_NAME);
        setHardness(5.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(((IExtendedBlockState) blockState.getBaseState())
                .withProperty(CAMOUFLAGE_STATE, null)
                .withProperty(ACTIVE, false)
                .withProperty(CAN_EMIT, false)
        );
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new IProperty[] { FACING, ACTIVE, OPEN_F, OPEN_B, OPEN_U, OPEN_D, OPEN_L, OPEN_R, CAN_EMIT },
                new IUnlistedProperty[] {CAMOUFLAGE_STATE});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null) {
            return ((IExtendedBlockState) state).withProperty(CAMOUFLAGE_STATE, router.getCamouflage());
        }
        return state;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null) {
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState camo = getCamoState(source, pos);
        return camo != null ? camo.getBoundingBox(source, pos) : super.getBoundingBox(state, source, pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
        IBlockState camo = getCamoState(worldIn, pos);
        return camo != null ? camo.getCollisionBoundingBox(worldIn, pos) : super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
        IBlockState camo = getCamoState(worldIn, pos);
        if (camo != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, camo.getBoundingBox(worldIn, pos));
        } else {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    private IBlockState getCamoState(IBlockAccess blockAccess, BlockPos pos) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(blockAccess, pos);
        return router != null ? router.getCamouflage() : null;
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
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        NBTTagCompound compound = stack.getTagCompound();
        if (router != null && compound != null) {
            ((ItemStackHandler) router.getModules()).deserializeNBT(compound.getCompoundTag(NBT_MODULES));
            ((ItemStackHandler) router.getUpgrades()).deserializeNBT(compound.getCompoundTag(NBT_UPGRADES));
            try {
                router.setRedstoneBehaviour(RouterRedstoneBehaviour.valueOf(compound.getString(NBT_REDSTONE_BEHAVIOUR)));
            } catch (IllegalArgumentException e) {
                router.setRedstoneBehaviour(RouterRedstoneBehaviour.ALWAYS);
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
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
            if (router != null) {
                router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityItemRouter();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState blockstate) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null) {
            InventoryUtils.dropInventoryItems(world, pos, router.getBuffer());
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
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null) {
            ItemStack stack = router.getBufferItemStack();
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

        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null) {
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            if (router.getModuleCount() > 0 || router.getSpeedUpgrades() > 0 || router.getStackUpgrades() > 0) {
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }
                NBTTagCompound compound = stack.getTagCompound();
                compound.setTag(NBT_MODULES, ((ItemStackHandler) router.getModules()).serializeNBT());
                compound.setTag(NBT_UPGRADES, ((ItemStackHandler) router.getUpgrades()).serializeNBT());
                compound.setString(NBT_REDSTONE_BEHAVIOUR, router.getRedstoneBehaviour().toString());
                compound.setInteger(NBT_MODULE_COUNT, router.getModuleCount());
                for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                    compound.setInteger(NBT_UPGRADE_COUNT + "." + type, router.getUpgradeCount(type));
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
        if (compound != null && compound.hasKey(NBT_MODULE_COUNT)) {
            list.add(I18n.format("itemText.misc.routerConfigured"));
            MiscUtil.appendMultiline(list, "itemText.misc.moduleCount", compound.getInteger(NBT_MODULE_COUNT));
            for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                int c = compound.getInteger(NBT_UPGRADE_COUNT + "." + type);
                if (c > 0) {
                    String name = I18n.format("item." + type.toString().toLowerCase() + "Upgrade.name");
                    list.add(I18n.format("itemText.misc.upgradeCount", name, c));
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
            if (router != null) {
                if (router.isPermitted(player) && !world.isRemote) {
                    player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, world, pos.getX(), pos.getY(), pos.getZ());
                } else if (!router.isPermitted(player) && world.isRemote) {
                    player.addChatMessage(new TextComponentTranslation("chatText.security.accessDenied"));
                    player.playSound(MRSoundEvents.error, 1.0f, 1.0f);
                }
            }
        }
        return true;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, data.getPos());
        if (router != null) {
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
                probeInfo.text(TextFormatting.WHITE + MiscUtil.translate("guiText.tooltip.redstone.label")
                        + ": " + TextFormatting.AQUA + MiscUtil.translate("guiText.tooltip.redstone." + router.getRedstoneBehaviour()));
            } else {
                probeInfo.text(MiscUtil.translate("chatText.security.accessDenied"));
            }
        }
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return state.getValue(BlockItemRouter.CAN_EMIT);
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(blockAccess, pos);
        if (router != null) {
            int l = router.getRedstoneLevel(side, false);
            return l < 0 ? super.getWeakPower(blockState, blockAccess, pos, side) : l;
        } else {
            return 0;
        }
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(blockAccess, pos);
        if (router != null) {
            int l = router.getRedstoneLevel(side, true);
            return l < 0 ? super.getStrongPower(blockState, blockAccess, pos, side) : l;
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

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(worldIn, pos);
        if (router != null) {
            router.checkForRedstonePulse();
        }
    }

}
