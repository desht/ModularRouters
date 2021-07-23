package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.ImmutableSet;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.IHasTranslationKey;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayer;

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
            "onBlockActivated", "use"
    );
    private final Set<String> ITEM_METHODS = ImmutableSet.of(
            "onItemUseFirst", // forge method, no SRG nam
            "onItemUse", "useOn",
            "onItemRightClick", "use"
    );
    private static final Set<Item> itemBlacklist = new HashSet<>();
    private static final Set<Block> blockBlacklist = new HashSet<>();

    public enum ActionType implements IHasTranslationKey {
        ITEM_OR_BLOCK(false),
        USE_ITEM_ON_ENTITY(true),
        ATTACK_ENTITY(true);

        private final boolean entity;

        ActionType(boolean entity) {
            this.entity = entity;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.activator.action." + this;
        }

        public static ActionType fromOldOrdinal(int ord) {
            return ord == 2 ? USE_ITEM_ON_ENTITY : ITEM_OR_BLOCK;
        }

        public boolean isEntityTarget() {
            return entity;
        }
    }

    public enum LookDirection implements IHasTranslationKey {
        LEVEL(0f),
        ABOVE(-45f),
        BELOW(45f);

        private final float pitch;

        LookDirection(float pitch) {
            this.pitch = pitch;
        }

        BlockPos offset(BlockPos pos, int dist) {
            switch (this) {
                case ABOVE: return pos.relative(Direction.UP, dist);
                case BELOW: return pos.relative(Direction.DOWN, dist);
                default: return pos;
            }
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.activator.direction." + this;
        }
    }

    public enum EntityMode implements IHasTranslationKey {
        NEAREST,
        RANDOM,
        ROUND_ROBIN;

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.activator.entityMode." + this;
        }
    }

    public CompiledActivatorModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        CompoundTag compound = stack.getTagElement(ModularRouters.MODID);
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
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        ItemStack stack = router.getBufferItemStack();

        if (itemBlacklist.contains(stack.getItem())) return false;

        // we'll allow an empty stack, since right-clicking with an empty hand is a valid operation
        if (!stack.isEmpty() && !getFilter().test(stack)) {
            return false;
        }

        RouterFakePlayer fakePlayer = router.getFakePlayer();
        Vec3 centre = Vec3.atCenterOf(router.getBlockPos());
        // place the fake player just outside the router, on the correct face
        fakePlayer.setPos(centre.x() + getFacing().getStepX() * 0.501, centre.y() + getFacing().getStepY() * 0.501, centre.z() + getFacing().getStepZ() * 0.501);
        fakePlayer.setShiftKeyDown(sneaking);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);

        boolean didWork = false;
        switch (actionType) {
            case ITEM_OR_BLOCK:
                didWork = doUseItem(router, fakePlayer);
                break;
            case USE_ITEM_ON_ENTITY:
                didWork = doUseItemOnEntity(router, fakePlayer);
                break;
            case ATTACK_ENTITY:
                didWork = doAttackEntity(router, fakePlayer);
                break;
        }

        if (didWork) {
            router.setBufferItemStack(fakePlayer.getMainHandItem());
            dropExtraItems(router, fakePlayer);
        }

        return didWork;
    }

    private boolean doUseItem(ModularRouterBlockEntity router, FakePlayer fakePlayer) {
        BlockPos pos = router.getBlockPos();
        Level world = router.getLevel();
        ItemStack stack = router.getBufferItemStack();
        fakePlayer.setYRot(MiscUtil.getYawFromFacing(getFacing()));
        fakePlayer.setXRot(getFacing().getAxis() == Direction.Axis.Y ? getFacing().getStepY() * -90 : lookDirection.pitch);
        BlockHitResult brtr = doRayTrace(pos, fakePlayer);
        BlockState state = world.getBlockState(brtr.getBlockPos());
        if (brtr.getType() != HitResult.Type.MISS && blockBlacklist.contains(state.getBlock())) {
            return false;
        }
        try {
            return fakePlayer.gameMode.useItemOn(fakePlayer, world, stack, InteractionHand.MAIN_HAND, brtr).consumesAction()
                    || fakePlayer.gameMode.useItem(fakePlayer, world, stack, InteractionHand.MAIN_HAND).consumesAction();
        } catch (Exception e) {
            handleBlacklisting(stack, state, e);
            return false;
        }
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

    private BlockHitResult doRayTrace(BlockPos routerPos, FakePlayer fp) {
        Vec3 fpVec = fp.position(); // ray trace starts at this point

        int xOff = getFacing().getStepX();
        int yOff = getFacing().getStepY();
        int zOff = getFacing().getStepZ();

        BlockPos.MutableBlockPos targetPos = routerPos.relative(getFacing()).mutable();
        if (lookDirection != LookDirection.LEVEL
                && Block.isShapeFullBlock(fp.level.getBlockState(targetPos).getShape(fp.level, targetPos)))
        {
            // small QoL kludge: if module faces horizontally AND is blocked on that side AND module looks above/below,
            // move the fake player pos above or below that block and target the top or bottom face as appropriate
            if (lookDirection == LookDirection.ABOVE) {
                fpVec = Vec3.atCenterOf(targetPos).add(0, 1, 0);
                yOff = -1;
            } else if (lookDirection == LookDirection.BELOW) {
                fpVec = Vec3.atCenterOf(targetPos).add(0, -1, 0);
                yOff = 1;
            }
        } else {
            if (lookDirection == LookDirection.ABOVE) {
                targetPos.move(Direction.UP);
                yOff = 1;
            } else if (lookDirection == LookDirection.BELOW) {
                targetPos.move(Direction.DOWN);
                yOff = -1;
            }
        }

        double reachDist = Math.pow(getPlayerReachDistance(fp), 2);
        for (; targetPos.distSqr(routerPos) <= reachDist; targetPos.move(xOff, yOff, zOff)) {
            if (fp.level.isEmptyBlock(targetPos)) continue;
            VoxelShape shape = fp.level.getBlockState(targetPos).getShape(fp.level, targetPos);
            if (shape.isEmpty()) continue;
            Vec3 targetVec = shape.toAabbs().get(0).getCenter().add(Vec3.atLowerCornerOf(targetPos));
            BlockHitResult res = fp.level.clip(new ClipContext(
                    fpVec, targetVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, null)
            );
            if (res.getType() == HitResult.Type.BLOCK) {
                return res;
            }
        }

        return BlockHitResult.miss(fpVec.add(fp.getLookAngle()), getFacing().getOpposite(), routerPos.relative(getFacing()));
    }

    private double getPlayerReachDistance(Player player) {
        if (player != null) {
            AttributeInstance attr = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
            if (attr != null) return attr.getValue() + 1D;
        }
        return 4.5D;
    }

    private boolean doAttackEntity(ModularRouterBlockEntity router, RouterFakePlayer fakePlayer) {
        LivingEntity entity = findEntity(router, LivingEntity.class);
        if (entity == null || entity instanceof Player && router.getUpgradeCount(ModItems.SECURITY_UPGRADE.get()) > 0 && router.isPermitted((Player) entity)) {
            return false;
        }
        fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, entity.position());
        fakePlayer.attack(entity);
        return true;
    }

    private boolean doUseItemOnEntity(ModularRouterBlockEntity router, FakePlayer fakePlayer) {
        Entity entity = findEntity(router, Entity.class);
        if (entity == null) {
            return false;
        }
        InteractionResult result = fakePlayer.interactOn(entity, InteractionHand.MAIN_HAND);
        if (result.consumesAction()) {
            router.setBufferItemStack(fakePlayer.getMainHandItem());
            return true;
        }
        return false;
    }

    private <T extends Entity> T findEntity(ModularRouterBlockEntity router, Class<T> cls) {
        Direction face = getFacing();
        final BlockPos pos = router.getBlockPos();
        Vec3 vec = Vec3.atCenterOf(pos);
        AABB box = new AABB(vec, vec)
                .move(face.getStepX() * 2.5, face.getStepY() * 2.5, face.getStepZ() * 2.5)
                .inflate(2.0);
        List<T> l = router.getLevel().getEntitiesOfClass(cls, box, this::passesBlacklist);
        if (l.isEmpty()) {
            return null;
        }

        switch (entityMode) {
            case RANDOM:
                return l.get(router.getLevel().random.nextInt(l.size()));
            case NEAREST:
                l.sort(Comparator.comparingDouble(o -> o.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())));
                return l.get(0);
            case ROUND_ROBIN:
                l.sort(Comparator.comparingDouble(o -> o.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())));
                entityIdx = (entityIdx + 1) % l.size();
                return l.get(entityIdx);
            default:
                return null;
        }
    }

    private boolean passesBlacklist(Entity e) {
        return !MRConfig.Common.Module.activatorEntityBlacklist.contains(e.getType().getRegistryName());
    }

    private void dropExtraItems(ModularRouterBlockEntity router, Player fakePlayer) {
        // any items added to the fake player's inventory from using the held item need to be dropped into
        // the world, since the router has no access to them, and the player would otherwise lose them
        // e.g. milking a cow with a stack of buckets in the router slot
        NonNullList<ItemStack> inv = fakePlayer.getInventory().items;
        Vec3 where = Vec3.atCenterOf(router.getBlockPos().relative(getFacing()));
        // start at slot 1, since slot 0 is always used for the fake player's held item, which doesn't get dropped
        for (int i = 1; i < inv.size() && !inv.get(i).isEmpty(); i++) {
            ItemEntity item = new ItemEntity(router.getLevel(), where.x(), where.y(), where.z(), inv.get(i));
            router.getLevel().addFreshEntity(item);
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

    @Override
    public int getEnergyCost() {
        return actionType == ActionType.ATTACK_ENTITY ?
                MRConfig.Common.EnergyCosts.activatorModuleEnergyCostAttack :
                MRConfig.Common.EnergyCosts.activatorModuleEnergyCost;
    }

    @Override
    public boolean careAboutItemAttributes() {
        return actionType == ActionType.ATTACK_ENTITY;
    }
}
