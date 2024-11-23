package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.network.messages.PushEntityMessage;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CompiledExtruderModule1 extends CompiledModule {
    public static final String NBT_EXTRUDER_DIST = "ExtruderDist";
    private static final double BASE_PUSH_STRENGTH = 0.55;
    private static final double AUGMENT_BOOST = 0.15;

    int distance;  // marks the current extension length (0 = no extrusion)
    private final int pushingAugments;
    private final ItemStack pickaxe;

    public CompiledExtruderModule1(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        distance = router == null ? 0 : router.getExtensionData().getInt(NBT_EXTRUDER_DIST + getAbsoluteFacing());
        pushingAugments = getAugmentCount(ModItems.PUSHING_AUGMENT);
        pickaxe = stack.getItem() instanceof IPickaxeUser p ? p.getPickaxe(stack) : ItemStack.EMPTY;
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        boolean extend = shouldExtend(router);
        Level world = router.nonNullLevel();
        Direction dir = Objects.requireNonNull(getAbsoluteFacing());  // should always be non-null at this point

        if (extend && !router.isBufferEmpty() && distance < getRange() && isRegulationOK(router, false)) {
            // try to extend
            BlockPos placePos = router.getBlockPos().relative(dir, distance + 1);
            ItemStack toPlace = router.peekBuffer(1);
            BlockState state = BlockUtil.tryPlaceAsBlock(router, toPlace, world, placePos, dir);
            if (state != null) {
                router.extractBuffer(1);
                router.getExtensionData().putInt(NBT_EXTRUDER_DIST + dir, ++distance);
                if (ConfigHolder.common.module.extruderSound.get()) {
                    router.playSound(null, placePos,
                            state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                            SoundSource.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                tryPushEntities(router.getLevel(), placePos, dir);
                return true;
            }
        } else if (!extend && distance > 0 && isRegulationOK(router, true)) {
            // try to retract
            BlockPos breakPos = router.getBlockPos().relative(dir, distance);
            BlockState oldState = world.getBlockState(breakPos);
            Block oldBlock = oldState.getBlock();
            if (world.isEmptyBlock(breakPos) || oldBlock instanceof LiquidBlock) {
                // nothing there? continue to retract anyway...
                router.getExtensionData().putInt(NBT_EXTRUDER_DIST + dir, --distance);
                return false;
            }
            if (BlockUtil.tryBreakBlock(router, world, breakPos, getFilter(), pickaxe, false)) {
                router.getExtensionData().putInt(NBT_EXTRUDER_DIST + dir, --distance);
                if (ConfigHolder.common.module.extruderSound.get()) {
                    router.playSound(null, breakPos,
                            oldBlock.getSoundType(oldState, world, breakPos, null).getBreakSound(),
                            SoundSource.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        }
        return false;
    }

    void tryPushEntities(Level world, BlockPos placePos, Direction facing) {
        if (!ConfigHolder.common.module.extruderPushEntities.get()) {
            return;
        }
        Vec3 v = Vec3.atLowerCornerOf(facing.getNormal()).scale(BASE_PUSH_STRENGTH + pushingAugments * AUGMENT_BOOST);
        for (Entity entity : world.getEntitiesOfClass(Entity.class, new AABB(placePos))) {
            if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                entity.setDeltaMovement(v);
                entity.setOnGround(false);
                entity.horizontalCollision = false;
                entity.verticalCollision = false;
                if (entity instanceof LivingEntity) ((LivingEntity) entity).setJumping(true);
                PacketDistributor.sendToPlayersTrackingEntity(entity, PushEntityMessage.forEntity(entity, v));
            }
        }
    }

    @Override
    public boolean checkRedstone(boolean powered, boolean pulsed) {
        return true;
    }

    boolean shouldExtend(ModularRouterBlockEntity router) {
        return switch (getRedstoneBehaviour()) {
            case ALWAYS -> router.getRedstonePower() > 0;
            case HIGH -> router.getRedstonePower() == 15;
            case LOW -> router.getRedstonePower() == 0;
            default -> false;
        };
    }
}
