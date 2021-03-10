package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.PushEntityMessage;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;

public class CompiledExtruderModule1 extends CompiledModule {
    public static final String NBT_EXTRUDER_DIST = "ExtruderDist";
    private static final double BASE_PUSH_STRENGTH = 0.55;
    private static final double AUGMENT_BOOST = 0.15;

    int distance;  // marks the current extension length (0 = no extrusion)
    private final int pushingAugments;
    private final ItemStack pickaxe;

    public CompiledExtruderModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        distance = router == null ? 0 : router.getExtData().getInt(NBT_EXTRUDER_DIST + getFacing());
        pushingAugments = getAugmentCount(ModItems.PUSHING_AUGMENT.get());
        pickaxe = stack.getItem() instanceof IPickaxeUser ? ((IPickaxeUser) stack.getItem()).getPickaxe(stack) : ItemStack.EMPTY;

        // backwards compat
        if (!EnchantmentHelper.getEnchantments(stack).isEmpty() && EnchantmentHelper.getEnchantments(pickaxe).isEmpty()) {
            EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(stack), pickaxe);
        }
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        boolean extend = shouldExtend(router);
        World world = router.getLevel();

        if (extend && !router.isBufferEmpty() && distance < getRange() && isRegulationOK(router, false)) {
            // try to extend
            BlockPos placePos = router.getBlockPos().relative(getFacing(), distance + 1);
            ItemStack toPlace = router.peekBuffer(1);
            BlockState state = BlockUtil.tryPlaceAsBlock(router, toPlace, world, placePos, getFacing());
            if (state != null) {
                router.extractBuffer(1);
                router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), ++distance);
                if (MRConfig.Common.Module.extruderSound) {
                    router.playSound(null, placePos,
                            state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                tryPushEntities(router.getLevel(), placePos, getFacing());
                return true;
            }
        } else if (!extend && distance > 0 && isRegulationOK(router, true)) {
            // try to retract
            BlockPos breakPos = router.getBlockPos().relative(getFacing(), distance);
            BlockState oldState = world.getBlockState(breakPos);
            Block oldBlock = oldState.getBlock();
            if (world.isEmptyBlock(breakPos) || oldBlock instanceof FlowingFluidBlock) {
                // nothing there? continue to retract anyway...
                router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), --distance);
                return false;
            }
            BlockUtil.BreakResult dropResult = BlockUtil.tryBreakBlock(router, world, breakPos, getFilter(), pickaxe);
            if (dropResult.isBlockBroken()) {
                router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), --distance);
                dropResult.processDrops(world, breakPos, router.getBuffer());
                if (MRConfig.Common.Module.extruderSound) {
                    router.playSound(null, breakPos,
                            oldBlock.getSoundType(oldState, world, breakPos, null).getBreakSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        }
        return false;
    }

    void tryPushEntities(World world, BlockPos placePos, Direction facing) {
        if (!MRConfig.Common.Module.extruderPushEntities) {
            return;
        }
        Vector3d v = Vector3d.atLowerCornerOf(facing.getNormal()).scale(BASE_PUSH_STRENGTH + pushingAugments * AUGMENT_BOOST);
        for (Entity entity : world.getEntitiesOfClass(Entity.class, new AxisAlignedBB(placePos))) {
            if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                entity.setDeltaMovement(v);
                entity.setOnGround(false);
                entity.horizontalCollision = false;
                entity.verticalCollision = false;
                if (entity instanceof LivingEntity) ((LivingEntity) entity).setJumping(true);
                PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 32, world.dimension());
                PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                        new PushEntityMessage(entity, v));
            }
        }
    }

    @Override
    public boolean shouldRun(boolean powered, boolean pulsed) {
        return true;
    }

    boolean shouldExtend(TileEntityItemRouter router) {
        switch (getRedstoneBehaviour()) {
            case ALWAYS:
                return router.getRedstonePower() > 0;
            case HIGH:
                return router.getRedstonePower() == 15;
            case LOW:
                return router.getRedstonePower() == 0;
            default:
                return false;
        }
    }
}
