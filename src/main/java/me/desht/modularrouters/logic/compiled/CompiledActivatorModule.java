package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.ImmutableSet;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayer;
import org.jline.utils.Log;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompiledActivatorModule extends CompiledModule {
    public static final String NBT_ACTION_TYPE_OLD = "ActionType";
    public static final String NBT_ACTION_TYPE = "ActionType2";
    public static final String NBT_LOOK_DIRECTION = "LookDirection";
    public static final String NBT_SNEAKING = "Sneaking";
    public static final String NBT_ENTITY_MODE = "EntityMode";

    private final ActionType actionType;
    private final LookDirection lookDirection;
    private final EntityMode entityMode;
    private final boolean sneaking;
    private int entityIdx;


    private final Set<String> BLOCK_METHODS = ImmutableSet.of(
            "onBlockActivated", "func_227031_a_"
    );
    private final Set<String> ITEM_METHODS = ImmutableSet.of(
            "onItemUseFirst", // forge method, no SRG nam
            "onItemUse", "func_195939_a",
            "onItemRightClick", "func_77659_a"
    );
    private static final Set<Item> itemBlacklist = new HashSet<>();
    private static final Set<Block> blockBlacklist = new HashSet<>();

    public enum ActionType {
        ITEM_OR_BLOCK,
        USE_ITEM_ON_ENTITY;

        public String getTranslationKey() {
            return "modularrouters.itemText.activator.action." + toString();
        }

        public static ActionType fromOldOrdinal(int ord) {
            return ord == 2 ? USE_ITEM_ON_ENTITY : ITEM_OR_BLOCK;
        }
    }

    public enum LookDirection {
        LEVEL(0f),
        ABOVE(-45f),
        BELOW(45f);

        private final float pitch;

        LookDirection(float pitch) {
            this.pitch = pitch;
        }

        BlockPos offset(BlockPos pos, int dist) {
            switch (this) {
                case ABOVE: return pos.offset(Direction.UP, dist);
                case BELOW: return pos.offset(Direction.DOWN, dist);
                default: return pos;
            }
        }

        public String getTranslationKey() {
            return "modularrouters.itemText.activator.direction." + toString();
        }
    }

    public enum EntityMode {
        NEAREST,
        RANDOM,
        ROUND_ROBIN;

        public String getTranslationKey() {
            return "modularrouters.itemText.activator.entityMode." + toString();
        }
    }

    public CompiledActivatorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        CompoundNBT compound = stack.getChildTag(ModularRouters.MODID);
        if (compound != null) {
            if (compound.contains(NBT_ACTION_TYPE_OLD)) {
                actionType = ActionType.fromOldOrdinal(compound.getInt(NBT_ACTION_TYPE_OLD));
            } else {
                actionType = ActionType.values()[compound.getInt(NBT_ACTION_TYPE)];
            }
            lookDirection = LookDirection.values()[compound.getInt(NBT_LOOK_DIRECTION)];
            entityMode = EntityMode.values()[compound.getInt(NBT_ENTITY_MODE)];
            sneaking = compound.getBoolean(NBT_SNEAKING);
        } else {
            actionType = ActionType.ITEM_OR_BLOCK;
            lookDirection = LookDirection.LEVEL;
            entityMode = EntityMode.NEAREST;
            sneaking = false;
        }
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();

        if (itemBlacklist.contains(stack.getItem())) return false;

        // we'll allow an empty stack, since right-clicking with an empty hand is a valid operation
        if (!stack.isEmpty() && !getFilter().test(stack)) {
            return false;
        }
        World world = router.getWorld();
        BlockPos pos = router.getPos();

        RouterFakePlayer fakePlayer = router.getFakePlayer();
        Vector3d centre = Vector3d.copyCentered(pos);
        // place the fake player just outside the router, on the correct face
        fakePlayer.setPosition(centre.getX() + getFacing().getXOffset() * 0.501, centre.getY() + getFacing().getYOffset() * 0.501, centre.getZ() + getFacing().getZOffset() * 0.501);
        fakePlayer.setSneaking(sneaking);
        fakePlayer.setHeldItem(Hand.MAIN_HAND, stack);

        boolean didWork = false;
        if (actionType == ActionType.USE_ITEM_ON_ENTITY) {
            didWork = doUseItemOnEntity(router, fakePlayer);
        } else {
            fakePlayer.rotationYaw = MiscUtil.getYawFromFacing(getFacing());
            fakePlayer.rotationPitch = getFacing().getAxis() == Direction.Axis.Y ? getFacing().getYOffset() * -90 : lookDirection.pitch;
            BlockRayTraceResult brtr = doRayTrace(pos, fakePlayer);
            BlockState state = world.getBlockState(brtr.getPos());
            if (brtr.getType() != RayTraceResult.Type.MISS && blockBlacklist.contains(state.getBlock())) {
                return false;
            }
            try {
                didWork = fakePlayer.interactionManager.func_219441_a(fakePlayer, world, stack, Hand.MAIN_HAND, brtr).isSuccessOrConsume();
                if (!didWork) {
                    didWork = fakePlayer.interactionManager.processRightClick(fakePlayer, world, stack, Hand.MAIN_HAND).isSuccessOrConsume();
                }
            } catch (Exception e) {
                handleBlacklisting(stack, state, e);
            }
        }

        if (didWork) {
            router.setBufferItemStack(fakePlayer.getHeldItemMainhand());
            dropExtraItems(router, fakePlayer);
        }

        return didWork;
    }

    private void handleBlacklisting(ItemStack stack, BlockState state, Exception e) {
        // look for (obfuscated) onItemUseFirst, onItemUse, onItemRightClick and onBlockActivated methods in stack trace
        // blacklist the relevant block or item, as appropriate
        for (StackTraceElement el : e.getStackTrace()) {
            if (ITEM_METHODS.contains(el.getMethodName())) {
                ModularRouters.LOGGER.error("Attempting to use item {} threw an exception. Blacklisting this item for the Activator Module until next server restart!", stack);
                ModularRouters.LOGGER.error("Stacktrace:", e);
                itemBlacklist.add(stack.getItem());
                return;
            } else if (BLOCK_METHODS.contains(el.getMethodName())) {
                ModularRouters.LOGGER.error("Attempting to use block {} threw an exception. Blacklisting this block for the Activator Module until next server restart!", state);
                ModularRouters.LOGGER.error("Stacktrace:", e);
                blockBlacklist.add(state.getBlock());
                return;
            }
        }
    }

    private BlockRayTraceResult doRayTrace(BlockPos routerPos, FakePlayer fp) {
        Vector3d fpVec = fp.getPositionVec();
        double reachDist = getPlayerReachDistance(fp);
        for (int i = 1; i < reachDist; i++) {
            BlockPos targetPos = lookDirection.offset(routerPos.offset(getFacing(), i), i);
            if (fp.world.isAirBlock(targetPos)) continue;
            VoxelShape shape = fp.world.getBlockState(targetPos).getShape(fp.world, targetPos);
            Vector3d targetVec = shape.isEmpty() ? Vector3d.copyCentered(targetPos) : shape.toBoundingBoxList().get(0).getCenter().add(Vector3d.copy(targetPos));
            BlockRayTraceResult res = fp.world.rayTraceBlocks(new RayTraceContext(
                    fpVec, targetVec, BlockMode.OUTLINE, FluidMode.SOURCE_ONLY, null)
            );
            if (res.getType() == RayTraceResult.Type.BLOCK) {
                return res;
            }
        }
        return BlockRayTraceResult.createMiss(fpVec.add(fp.getLookVec()), getFacing().getOpposite(), routerPos.offset(getFacing()));
    }

    private double getPlayerReachDistance(PlayerEntity player) {
        if (player != null) {
            ModifiableAttributeInstance attr = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
            if (attr != null) return attr.getValue() + 1D;
        }
        return 4.5D;
    }

    private boolean doUseItemOnEntity(TileEntityItemRouter router, FakePlayer fakePlayer) {
        Entity entity = findEntity(router);
        if (entity == null) {
            return false;
        }
        ActionResultType result = fakePlayer.interactOn(entity, Hand.MAIN_HAND);
        if (result.isSuccessOrConsume()) {
            router.setBufferItemStack(fakePlayer.getHeldItemMainhand());
            return true;
        }
        return false;
    }

    private Entity findEntity(TileEntityItemRouter router) {
        Direction face = getFacing();
        final BlockPos pos = router.getPos();
        AxisAlignedBB box = new AxisAlignedBB(pos.offset(face))
                .expand(face.getXOffset() * 3, face.getYOffset() * 3, face.getZOffset() * 3);

        List<Entity> l = router.getWorld().getEntitiesWithinAABB(Entity.class, box, this::passesBlacklist);
        if (l.isEmpty()) {
            return null;
        }

        switch (entityMode) {
            case RANDOM:
                return l.get(router.getWorld().rand.nextInt(l.size()));
            case NEAREST:
                l.sort(Comparator.comparingDouble(o -> o.getDistanceSq(pos.getX(), pos.getY(), pos.getZ())));
                return l.get(0);
            case ROUND_ROBIN:
                l.sort(Comparator.comparingDouble(o -> o.getDistanceSq(pos.getX(), pos.getY(), pos.getZ())));
                entityIdx = (entityIdx + 1) % l.size();
                return l.get(entityIdx);
            default:
                return null;
        }
    }

    private boolean passesBlacklist(Entity e) {
        return !MRConfig.Common.Module.activatorEntityBlacklist.contains(e.getType().getRegistryName());
    }

    private void dropExtraItems(TileEntityItemRouter router, PlayerEntity fakePlayer) {
        // any items added to the fake player's inventory from using the held item need to be dropped into
        // the world, since the router has no access to them, and the player would otherwise lose them
        // e.g. milking a cow with a stack of buckets in the router slot
        NonNullList<ItemStack> inv = fakePlayer.inventory.mainInventory;
        Vector3d where = Vector3d.copyCentered(router.getPos().offset(getFacing()));
        // start at slot 1, since slot 0 is always used for the fake player's held item, which doesn't get dropped
        for (int i = 1; i < inv.size() && !inv.get(i).isEmpty(); i++) {
            ItemEntity item = new ItemEntity(router.getWorld(), where.getX(), where.getY(), where.getZ(), inv.get(i));
            router.getWorld().addEntity(item);
            inv.set(i, ItemStack.EMPTY);
        }
    }

    public ActionType getActionType() {
        return actionType;
    }

    public LookDirection getLookDirection() {
        return lookDirection;
    }

    public EntityMode getEntityMode() {
        return entityMode;
    }

    public boolean isSneaking() {
        return sneaking;
    }
}
