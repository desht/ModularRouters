package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.RouterSettingsMessage;
import me.desht.modularrouters.network.RouterUpgradesSyncMessage;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static me.desht.modularrouters.block.tile.TileEntityItemRouter.*;

public class BlockItemRouter extends BlockCamo {
    private static final float HARDNESS = 1.5f;
    private static final float BLAST_RESISTANCE = 6.0f;

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty CAN_EMIT = BooleanProperty.create("can_emit");

    public BlockItemRouter() {
        super(Block.Properties.of(Material.METAL)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.METAL));
        registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH).setValue(ACTIVE, false).setValue(CAN_EMIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, CAN_EMIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        Direction enumfacing = (ctx.getPlayer() == null) ? ctx.getClickedFace() : Direction.fromYRot(ctx.getPlayer().yRot).getOpposite();
        return this.defaultBlockState().setValue(FACING, enumfacing);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return VoxelShapes.block();
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityItemRouter();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntityItemRouter.getRouterAt(world, pos).ifPresent(router -> {
                InventoryUtils.dropInventoryItems(world, pos, router.getBuffer());
                world.updateNeighbourForOutputSignal(pos, this);
                super.onRemove(state, world, pos, newState, isMoving);
            });
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        return TileEntityItemRouter.getRouterAt(world, pos)
                .map(router -> ItemHandlerHelper.calcRedstoneFromInventory(router.getBuffer()))
                .orElse(0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader player, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        if (stack.hasTag()) {
            CompoundNBT compound = stack.getTag().getCompound("BlockEntityTag");
            tooltip.add(ClientUtil.xlate("modularrouters.itemText.misc.routerConfigured")
                    .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
            if (compound.contains(NBT_MODULES)) {
                List<ITextComponent> moduleText = new ArrayList<>();
                ItemStackHandler modulesHandler = new ItemStackHandler(9);
                modulesHandler.deserializeNBT(compound.getCompound(NBT_MODULES));
                for (int i = 0; i < modulesHandler.getSlots(); i++) {
                    ItemStack moduleStack = modulesHandler.getStackInSlot(i);
                    if (!moduleStack.isEmpty()) {
                        moduleText.add(new StringTextComponent("\u2022 ")
                                .append(moduleStack.getHoverName())
                                .withStyle(TextFormatting.AQUA)
                        );
                    }
                }
                if (!moduleText.isEmpty()) {
                    tooltip.add(ClientUtil.xlate("modularrouters.itemText.misc.moduleCount",
                            moduleText.size()).withStyle(TextFormatting.YELLOW));
                    tooltip.addAll(moduleText);
                }
            }
            if (compound.contains(NBT_UPGRADES)) {
                ItemStackHandler upgradesHandler = new ItemStackHandler();
                upgradesHandler.deserializeNBT(compound.getCompound(NBT_UPGRADES));
                List<ITextComponent> upgradeText = new ArrayList<>();
                int nUpgrades = 0;
                for (int i = 0; i < upgradesHandler.getSlots(); i++) {
                    ItemStack upgradeStack = upgradesHandler.getStackInSlot(i);
                    if (!upgradeStack.isEmpty()) {
                        nUpgrades += upgradeStack.getCount();
                        upgradeText.add(new StringTextComponent("\u2022 " + upgradeStack.getCount() + " x ")
                                .append(upgradeStack.getHoverName())
                                .withStyle(TextFormatting.AQUA)
                        );
                    }
                }
                if (!upgradeText.isEmpty()) {
                    tooltip.add(ClientUtil.xlate("modularrouters.itemText.misc.upgradeCount", nUpgrades).withStyle(TextFormatting.YELLOW));
                    tooltip.addAll(upgradeText);
                }
            }
            if (compound.contains(NBT_REDSTONE_MODE)) {
                try {
                    RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.valueOf(compound.getString(NBT_REDSTONE_MODE));
                    tooltip.add(ClientUtil.xlate("modularrouters.guiText.tooltip.redstone.label")
                            .append(": ")
                            .withStyle(TextFormatting.YELLOW)
                            .append(ClientUtil.xlate("modularrouters.guiText.tooltip.redstone." + rrb)
                                    .withStyle(TextFormatting.RED))
                    );
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (!player.isSteppingCarefully()) {
            return TileEntityItemRouter.getRouterAt(world, pos).map(router -> {
                if (router.isPermitted(player) && !world.isClientSide) {
                    // TODO combine into one packet?
                    PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new RouterSettingsMessage(router));
                    PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new RouterUpgradesSyncMessage(router));
                    NetworkHooks.openGui((ServerPlayerEntity) player, router, pos);
                } else if (!router.isPermitted(player) && world.isClientSide) {
                    player.displayClientMessage(ClientUtil.xlate("modularrouters.chatText.security.accessDenied"), false);
                    player.playSound(ModSounds.ERROR.get(), 1.0f, 1.0f);
                }
                return ActionResultType.SUCCESS;
            }).orElse(ActionResultType.FAIL);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(BlockItemRouter.CAN_EMIT);
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return TileEntityItemRouter.getRouterAt(blockAccess, pos).map(router -> {
            int l = router.getRedstoneLevel(side, false);
            return l < 0 ? super.getSignal(blockState, blockAccess, pos, side) : l;
        }).orElse(0);
    }

    @Override
    public int getDirectSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return TileEntityItemRouter.getRouterAt(blockAccess, pos).map(router -> {
            int l = router.getRedstoneLevel(side, true);
            return l < 0 ? super.getDirectSignal(blockState, blockAccess, pos, side) : l;
        }).orElse(0);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean b) {
        TileEntityItemRouter.getRouterAt(worldIn, pos).ifPresent(router -> {
            router.checkForRedstonePulse();
            router.notifyModules();
        });
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        return TileEntityItemRouter.getRouterAt(world, pos)
                .map(router -> router.getUpgradeCount(ModItems.BLAST_UPGRADE.get()) <= 0 && super.canEntityDestroy(state, world, pos, entity))
                .orElse(true);
    }

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return TileEntityItemRouter.getRouterAt(world, pos)
                .map(router -> router.getUpgradeCount(ModItems.BLAST_UPGRADE.get()) > 0 ? 20000f : BLAST_RESISTANCE)
                .orElse(0f);
    }
}
