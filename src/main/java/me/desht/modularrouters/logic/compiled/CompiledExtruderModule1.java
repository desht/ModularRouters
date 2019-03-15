package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.PushEntityMessage;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;

public class CompiledExtruderModule1 extends CompiledModule {
    public static final String NBT_EXTRUDER_DIST = "ExtruderDist";
    private static final double BASE_PUSH_STRENGTH = 0.55;
    private static final double AUGMENT_BOOST = 0.15;

    int distance;  // marks the current extension length (0 = no extrusion)
    private final boolean silkTouch;
    private final int pushingAugments;

    public CompiledExtruderModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        distance = router == null ? 0 : router.getExtData().getInt(NBT_EXTRUDER_DIST + getFacing());
        silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
        pushingAugments = getAugmentCount(ObjectRegistry.PUSHING_AUGMENT);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        boolean extend = shouldExtend(router);
        World world = router.getWorld();

        if (extend && !router.isBufferEmpty() && distance < getRange() && isRegulationOK(router, false)) {
            // try to extend
            BlockPos placePos = router.getPos().offset(getFacing(), distance + 1);
            ItemStack toPlace = router.peekBuffer(1);
            IBlockState state = BlockUtil.tryPlaceAsBlock(toPlace, world, placePos, getFacing(), getRouterFacing());
            if (state != null) {
                router.extractBuffer(1);
                router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), ++distance);
                if (ConfigHandler.MODULE.extruderSound.get()) {
                    router.playSound(null, placePos,
                            state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                tryPushEntities(router.getWorld(), placePos, getFacing());
                return true;
            }
        } else if (!extend && distance > 0 && isRegulationOK(router, true)) {
            // try to retract
            BlockPos breakPos = router.getPos().offset(getFacing(), distance);
            IBlockState oldState = world.getBlockState(breakPos);
            Block oldBlock = oldState.getBlock();
            if (world.isAirBlock(breakPos) /*|| oldBlock instanceof BlockLiquid*/ || oldBlock instanceof IFluidBlock) {
                // nothing there? continue to retract anyway...
                router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), --distance);
                return false;
            }
            BlockUtil.BreakResult dropResult = BlockUtil.tryBreakBlock(world, breakPos, getFilter(), silkTouch, 0);
            if (dropResult.isBlockBroken()) {
                router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), --distance);
                dropResult.processDrops(world, breakPos, router.getBuffer());
                if (ConfigHandler.MODULE.extruderSound.get()) {
                    router.playSound(null, breakPos,
                            oldBlock.getSoundType(oldState, world, breakPos, null).getBreakSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        }
        return false;
    }

    void tryPushEntities(World world, BlockPos placePos, EnumFacing facing) {
        if (!ConfigHandler.MODULE.extruderPushEntities.get()) {
            return;
        }
        Vec3d v = new Vec3d(facing.getDirectionVec()).scale(BASE_PUSH_STRENGTH + pushingAugments * AUGMENT_BOOST);
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(placePos))) {
            if (entity.getPushReaction() != EnumPushReaction.IGNORE) {
                entity.motionX = v.x;
                entity.motionY = v.y;
                entity.motionZ = v.z;
                entity.onGround = false;
                entity.collided = false;
                entity.collidedHorizontally = false;
                entity.collidedVertically = false;
                if (entity instanceof EntityLivingBase) ((EntityLivingBase) entity).setJumping(true);
                PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(entity.posX, entity.posY, entity.posZ, 32, world.dimension.getType());
                PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                        new PushEntityMessage(entity, v.x, v.y, v.z));
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
