package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.*;

public class CompiledActivatorModule extends CompiledModule {
    public static final String NBT_ACTION_TYPE = "ActionType";
    public static final String NBT_LOOK_DIRECTION = "LookDirection";
    public static final String NBT_SNEAKING = "Sneaking";
    public static final String NBT_ENTITY_MODE = "EntityMode";

    private final ActionType actionType;
    private final LookDirection lookDirection;
    private final EntityMode entityMode;
    private final boolean sneaking;
    private int entityIdx;

    private static final Set<Item> itemBlacklist = new HashSet<>();
    private static final Set<Block> blockBlacklist = new HashSet<>();

    public enum ActionType {
        ACTIVATE_BLOCK,
        USE_ITEM,
        USE_ITEM_ON_ENTITY;

        public String getTranslationKey() {
            return "modularrouters.itemText.activator.action." + toString();
        }
    }

    public enum LookDirection {
        LEVEL(Vector3d.ZERO),
        ABOVE(new Vector3d(0, 1, 0)),
        BELOW(new Vector3d(0, -1, 0));

        private final Vector3d offsetVec;

        LookDirection(Vector3d offsetVec) {
            this.offsetVec = offsetVec;
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
            actionType = ActionType.values()[compound.getInt(NBT_ACTION_TYPE)];
            lookDirection = LookDirection.values()[compound.getInt(NBT_LOOK_DIRECTION)];
            entityMode = EntityMode.values()[compound.getInt(NBT_ENTITY_MODE)];
            sneaking = compound.getBoolean(NBT_SNEAKING);
        } else {
            actionType = ActionType.ACTIVATE_BLOCK;
            lookDirection = LookDirection.LEVEL;
            entityMode = EntityMode.NEAREST;
            sneaking = false;
        }
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();
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
        fakePlayer.rotationPitch = getFacing().getYOffset() * -90;
        fakePlayer.rotationYaw = MiscUtil.getYawFromFacing(getFacing());
        fakePlayer.setSneaking(sneaking);
        fakePlayer.setHeldItem(Hand.MAIN_HAND, stack);

        boolean didWork = false;
        switch (actionType) {
            case ACTIVATE_BLOCK:
                didWork = doActivateBlock(router, world, fakePlayer);
                break;
            case USE_ITEM:
                didWork = doUseItem(router, computeItemUseContext(fakePlayer, router.getPos()));
                break;
            case USE_ITEM_ON_ENTITY:
                didWork = doUseItemOnEntity(router, fakePlayer);
                break;
        }

        if (didWork) dropExtraItems(router, fakePlayer);

        return didWork;
    }

    private ItemUseContext computeItemUseContext(FakePlayer fakePlayer, BlockPos routerPos) {
        BlockPos targetPos = routerPos.offset(getFacing());
        ServerWorld world = fakePlayer.getServerWorld();
        Vector3d centre = Vector3d.copyCentered(routerPos);
        Vector3d fpVec = new Vector3d(centre.getX() + getFacing().getXOffset() * 0.501, centre.getY() + getFacing().getYOffset() * 0.501, centre.getZ() + getFacing().getZOffset() * 0.501);
        switch (lookDirection) {
            case LEVEL:
                if (world.isAirBlock(targetPos)) {
                    BlockPos newPos = null;
                    for (int i = 0; i < 4; i++) {
                        BlockPos p = targetPos.offset(getFacing(), i);
                        if (!world.isAirBlock(p)) {
                            newPos = p;
                            break;
                        }
                    }
                    targetPos = newPos == null ? targetPos.offset(getFacing(), 3) : newPos.offset(getFacing().getOpposite());
                }
                break;
            case ABOVE:
                targetPos = targetPos.up();
                fpVec = fpVec.add(0, 1, 0);
                break;
            case BELOW:
                targetPos = targetPos.down();
                fpVec = fpVec.add(0, -1, 0);
                break;
        }

        for (Direction dir : Direction.values()) {
            if (dir.equals(getFacing())) continue;
            BlockPos newTarget = targetPos.offset(dir);
            if (!world.isAirBlock(newTarget)) {
                fpVec = Vector3d.copyCentered(targetPos);
                targetPos = newTarget;
                break;
            }
        }

        VoxelShape shape = world.getBlockState(targetPos).getShape(world, targetPos);
        Vector3d targetVec = shape.isEmpty() ? Vector3d.copyCentered(targetPos) : shape.getBoundingBox().getCenter().add(Vector3d.copy(targetPos));
        BlockRayTraceResult brtr = world.rayTraceBlocks(new RayTraceContext(fpVec, targetVec, BlockMode.OUTLINE, FluidMode.NONE, fakePlayer));

        return new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr);
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

    private boolean doUseItem(TileEntityItemRouter router, ItemUseContext ctx) {
        PlayerEntity player = Objects.requireNonNull(ctx.getPlayer());

        ItemStack stack = ctx.getItem();
        if (itemBlacklist.contains(stack.getItem())) return false;

        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, Hand.MAIN_HAND, ctx.getPos(), ctx.getFace());
        if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
            return false;
        }

        try {
            ActionResultType ret = stack.onItemUseFirst(ctx);
            if (ret != ActionResultType.PASS) {
                return ret.isSuccessOrConsume();
            }

            if (stack.isEmpty() || player.getCooldownTracker().hasCooldown(stack.getItem())) {
                return false;
            }

            ActionResultType result;

            if (stack.getItem() instanceof BlockItem && !ctx.getPlayer().canUseCommandBlock()) {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (block instanceof CommandBlockBlock || block instanceof StructureBlock) {
                    return false;
                }
            }

            if (event.getUseItem() != Event.Result.DENY) {
                ItemStack copyBeforeUse = stack.copy();

                result = stack.onItemUse(ctx);
                if (result == ActionResultType.PASS) {
                    ActionResult<ItemStack> rightClickResult = stack.getItem().onItemRightClick(player.world, player, Hand.MAIN_HAND);
                    ctx.getPlayer().setHeldItem(Hand.MAIN_HAND, rightClickResult.getResult());
                }
                if (ctx.getPlayer().getHeldItem(Hand.MAIN_HAND).isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(ctx.getPlayer(), copyBeforeUse, Hand.MAIN_HAND);
                }

                router.setBufferItemStack(player.getHeldItemMainhand());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            ModularRouters.LOGGER.error("Attempting to use item {} threw an exception. Blacklisting this item for the Activator Module until next server restart!", stack);
            ModularRouters.LOGGER.error("Stacktrace:", e);
            itemBlacklist.add(stack.getItem());
            return false;
        }
    }

    private boolean doActivateBlock(TileEntityItemRouter router, World world, PlayerEntity fakePlayer) {
        BlockPos targetPos = findBlockToActivate(router);
        if (targetPos == null) {
            return false;
        }
        Direction hitFace = getFacing().getOpposite();

        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, targetPos, hitFace);
        if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
            return false;
        }
        if (event.getUseBlock() != Event.Result.DENY) {
            BlockState state = world.getBlockState(targetPos);
            if (blockBlacklist.contains(state.getBlock())) return false;
            try {
                BlockRayTraceResult rtr = rayTrace(world, fakePlayer.getEyePosition(1f).add(lookDirection.offsetVec), targetPos);
                if (rtr.getPos().equals(targetPos) && state.onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, rtr).isSuccessOrConsume()) {
                    router.setBufferItemStack(fakePlayer.getHeldItemMainhand());
                    return true;
                }
            } catch (Exception e) {
                ModularRouters.LOGGER.error("Attempting to activate block {} @ {} threw an exception. Blacklisting this block for the Activator Module until next server restart!", state, targetPos);
                ModularRouters.LOGGER.error("Stacktrace:", e);
                blockBlacklist.add(state.getBlock());
            }
        }
        return false;
    }

    private BlockPos findBlockToActivate(TileEntityItemRouter router) {
        switch (lookDirection) {
            case LEVEL:
                for (int i = 1; i < 5; i++) {
                    BlockPos pos = router.getPos().offset(getFacing(), i);
                    if (!router.getWorld().isAirBlock(pos)) {
                        return pos;
                    }
                }
                break;
            case ABOVE:
                return router.getPos().offset(getFacing()).up();
            case BELOW:
                return router.getPos().offset(getFacing()).down();
        }
        return null;
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

    private static BlockRayTraceResult rayTrace(World worldIn, Vector3d srcVec, BlockPos targetPos) {
        VoxelShape shape = worldIn.getBlockState(targetPos).getShape(worldIn, targetPos);
        Vector3d vec2 = shape.isEmpty() ? Vector3d.copyCentered(targetPos) : shape.getBoundingBox().getCenter().add(Vector3d.copy(targetPos));
        return worldIn.rayTraceBlocks(new RayTraceContext(
                srcVec, vec2, BlockMode.OUTLINE, FluidMode.NONE, null)
        );
    }
}
