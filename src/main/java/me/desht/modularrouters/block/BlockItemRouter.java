package me.desht.modularrouters.block;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockItemRouter extends BlockCamo {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty CAN_EMIT = BooleanProperty.create("can_emit");

    private static final String NBT_MODULES = "Modules";
    private static final String NBT_UPGRADES = "Upgrades";
    private static final String NBT_REDSTONE_BEHAVIOUR = "RedstoneBehaviour";

    public BlockItemRouter() {
        super(Block.Properties.create(Material.IRON)
                .hardnessAndResistance(5.0f)
                .sound(SoundType.METAL));
        setDefaultState(this.getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH).with(ACTIVE, false).with(CAN_EMIT, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, CAN_EMIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        Direction enumfacing = (ctx.getPlayer() == null) ? ctx.getFace() : Direction.fromAngle(ctx.getPlayer().rotationYaw).getOpposite();
        return this.getDefaultState().with(FACING, enumfacing);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        CompoundNBT compound = stack.getTag();
        if (router != null && compound != null) {
            ((ItemStackHandler) router.getModules()).deserializeNBT(compound.getCompound(NBT_MODULES));
            ((ItemStackHandler) router.getUpgrades()).deserializeNBT(compound.getCompound(NBT_UPGRADES));
            try {
                router.setRedstoneBehaviour(RouterRedstoneBehaviour.valueOf(compound.getString(NBT_REDSTONE_BEHAVIOUR)));
            } catch (IllegalArgumentException e) {
                router.setRedstoneBehaviour(RouterRedstoneBehaviour.ALWAYS);
            }
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityItemRouter();
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
            if (router != null) {
                InventoryUtils.dropInventoryItems(world, pos, router.getBuffer());
                world.updateComparatorOutputLevel(pos, this);
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null) {
            ItemStack stack = router.getBufferItemStack();
            return stack.isEmpty() ? 0 : MathHelper.floor(1 + ((float) stack.getCount() / (float) stack.getMaxStackSize()) * 14);
        } else {
            return 0;
        }
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        // If it will harvest, delay deletion of the block until after getDrops()
        return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluid);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);
        ItemStack stack = new ItemStack(this);
        if (te instanceof TileEntityItemRouter) {
            TileEntityItemRouter router = (TileEntityItemRouter) te;
            if (router.getModuleCount() > 0 || router.getUpgradeCount() > 0 || router.getRedstoneBehaviour() != RouterRedstoneBehaviour.ALWAYS) {
                CompoundNBT compound = stack.getOrCreateTag();
                if (router.getModuleCount() > 0) compound.put(NBT_MODULES, ((ItemStackHandler) router.getModules()).serializeNBT());
                if (router.getUpgradeCount() > 0) compound.put(NBT_UPGRADES, ((ItemStackHandler) router.getUpgrades()).serializeNBT());
                compound.putString(NBT_REDSTONE_BEHAVIOUR, router.getRedstoneBehaviour().toString());
            }
        }
        return ImmutableList.of(stack);
    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.removeBlock(pos, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader player, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        CompoundNBT compound = stack.getTag();
        if (compound == null) return;

        tooltip.add(new TranslationTextComponent("itemText.misc.routerConfigured")
                .applyTextStyles(TextFormatting.GRAY, TextFormatting.ITALIC));
        if (compound.contains(NBT_MODULES)) {
            List<ITextComponent> moduleText = new ArrayList<>();
            ItemStackHandler modulesHandler = new ItemStackHandler(9);
            modulesHandler.deserializeNBT(compound.getCompound(NBT_MODULES));
            for (int i = 0; i < modulesHandler.getSlots(); i++) {
                ItemStack moduleStack = modulesHandler.getStackInSlot(i);
                if (!moduleStack.isEmpty()) {
                    moduleText.add(new StringTextComponent("\u2022 ")
                            .appendSibling(moduleStack.getDisplayName())
                            .applyTextStyle(TextFormatting.AQUA)
                    );
                }
            }
            if (!moduleText.isEmpty()) {
                tooltip.add(new TranslationTextComponent("itemText.misc.moduleCount",
                        moduleText.size()).applyTextStyles(TextFormatting.YELLOW));
                tooltip.addAll(moduleText);
            }
        }
        if (compound.contains(NBT_UPGRADES)) {
            ItemStackHandler upgradesHandler = new ItemStackHandler();
            upgradesHandler.deserializeNBT(compound.getCompound(NBT_UPGRADES));
            List<ITextComponent> upgradeText = new ArrayList<>();
            for (int i = 0; i < upgradesHandler.getSlots(); i++) {
                ItemStack upgradeStack = upgradesHandler.getStackInSlot(i);
                if (!upgradeStack.isEmpty()) {
                    upgradeText.add(new StringTextComponent("\u2022 " + upgradeStack.getCount() + " x ")
                            .appendSibling(upgradeStack.getDisplayName())
                            .applyTextStyle(TextFormatting.AQUA)
                    );
                }
            }
            if (!upgradeText.isEmpty()) {
                tooltip.add(new TranslationTextComponent("itemText.misc.upgradeCount",
                        upgradeText.size()).applyTextStyles(TextFormatting.YELLOW));
                tooltip.addAll(upgradeText);
            }
        }
        if (compound.contains(NBT_REDSTONE_BEHAVIOUR)) {
            try {
                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.valueOf(compound.getString(NBT_REDSTONE_BEHAVIOUR));
                tooltip.add(new TranslationTextComponent("guiText.tooltip.redstone.label")
                        .appendText(": ")
                        .applyTextStyle(TextFormatting.YELLOW)
                        .appendSibling(new TranslationTextComponent("guiText.tooltip.redstone." + rrb)
                        .applyTextStyle(TextFormatting.RED))
                );
            } catch (IllegalArgumentException ignored) {
                // bad value for NBT_REDSTONE_BEHAVIOUR tag
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        if (!player.isSneaking()) {
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
            if (router != null) {
                if (router.isPermitted(player) && !world.isRemote) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, router, pos);
                } else if (!router.isPermitted(player) && world.isRemote) {
                    player.sendStatusMessage(new TranslationTextComponent("chatText.security.accessDenied"), false);
                    player.playSound(ModSounds.ERROR, 1.0f, 1.0f);
                }
            }
        }
        return true;
    }




    @Override
    public boolean canProvidePower(BlockState state) {
        return state.get(BlockItemRouter.CAN_EMIT);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(blockAccess, pos);
        if (router != null) {
            int l = router.getRedstoneLevel(side, false);
            return l < 0 ? super.getWeakPower(blockState, blockAccess, pos, side) : l;
        } else {
            return 0;
        }
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(blockAccess, pos);
        if (router != null) {
            int l = router.getRedstoneLevel(side, true);
            return l < 0 ? super.getStrongPower(blockState, blockAccess, pos, side) : l;
        } else {
            return 0;
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean b) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(worldIn, pos);
        if (router != null) {
            router.checkForRedstonePulse();
            router.notifyModules();
        }
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        return (router == null || router.getUpgradeCount(ModItems.BLAST_UPGRADE) <= 0)
                && super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null && router.getUpgradeCount(ModItems.BLAST_UPGRADE) > 0) {
            return 20000f;
        } else {
            return super.getExplosionResistance(state, world, pos, exploder, explosion);
        }
    }
}
