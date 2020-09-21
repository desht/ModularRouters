package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.FakePlayerManager;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

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

    public enum ActionType {
        ACTIVATE_BLOCK,
        USE_ITEM,
        USE_ITEM_ON_ENTITY
    }

    public enum LookDirection {
        LEVEL,
        ABOVE,
        BELOW
    }

    public enum EntityMode {
        NEAREST,
        RANDOM,
        ROUND_ROBIN
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
        if (!stack.isEmpty() && !getFilter().test(stack)) {
            // allow running with no item in buffer, since right-clicking with an empty hand is valid
            return false;
        }
        World world = router.getWorld();
        BlockPos pos = router.getPos();

        FakePlayer fakePlayer = FakePlayerManager.getFakePlayer((ServerWorld) world, pos);
        if (fakePlayer == null) {
            return false;
        }
        fakePlayer.setPosition(pos.getX() + 0.5, pos.getY() + 0.5 - fakePlayer.getEyeHeight(), pos.getZ() + 0.5);
        fakePlayer.rotationPitch = getFacing().getYOffset() * -90;
        fakePlayer.rotationYaw = MiscUtil.getYawFromFacing(getFacing());
        fakePlayer.setSneaking(sneaking);
        fakePlayer.setHeldItem(Hand.MAIN_HAND, stack);
        float hitX = (float)(fakePlayer.getPosX() - pos.getX());
        float hitY = (float)(fakePlayer.getPosY() - pos.getY());
        float hitZ = (float)(fakePlayer.getPosZ() - pos.getZ());

        switch (actionType) {
            case ACTIVATE_BLOCK:
                return doActivateBlock(router, world, fakePlayer, hitX, hitY, hitZ);
            case USE_ITEM:
                return doUseItem(router, world, pos, fakePlayer, hitX, hitY, hitZ);
            case USE_ITEM_ON_ENTITY:
                return doUseItemOnEntity(router, fakePlayer);
            default:
                return false;
        }
    }

    private boolean doUseItemOnEntity(TileEntityItemRouter router, FakePlayer fakePlayer) {
        Entity entity = findEntity(router);
        if (entity == null) {
            return false;
        }
        ActionResultType result = fakePlayer.interactOn(entity, Hand.MAIN_HAND);
        if (result == ActionResultType.SUCCESS) {
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

        List<Entity> l = router.getWorld().getEntitiesWithinAABB(Entity.class, box);
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

    private boolean doUseItem(TileEntityItemRouter router, World world, BlockPos pos, PlayerEntity fakePlayer, float hitX, float hitY, float hitZ) {
        BlockPos targetPos = pos.offset(getFacing());
        Direction hitFace = getHitFace();
        switch (lookDirection) {
            case LEVEL:
                for (int i = 0; i < 4 && world.isAirBlock(targetPos); i++) {
                    targetPos = targetPos.offset(getFacing());
                }
                break;
            case ABOVE:
                targetPos = targetPos.up();
                break;
            case BELOW:
                targetPos = targetPos.down();
                break;
        }

        ItemStack stack = fakePlayer.getHeldItemMainhand();
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, targetPos, hitFace);
        if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
            return false;
        }

        BlockRayTraceResult brtr = new BlockRayTraceResult(new Vec3d(hitX, hitY, hitZ), hitFace, targetPos, false);
        ActionResultType ret = stack.onItemUseFirst(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr));
        if (ret != ActionResultType.PASS) return false;

        if (stack.isEmpty() || fakePlayer.getCooldownTracker().hasCooldown(stack.getItem())) {
            return false;
        }

        ActionResultType result;

        if (stack.getItem() instanceof BlockItem && !fakePlayer.canUseCommandBlock()) {
            Block block = ((BlockItem)stack.getItem()).getBlock();
            if (block instanceof CommandBlockBlock || block instanceof StructureBlock) {
                return false;
            }
        }

        if (event.getUseItem() != Event.Result.DENY) {
            ItemStack copyBeforeUse = stack.copy();

            result = stack.onItemUse(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr));
            if (result == ActionResultType.PASS) {
                ActionResult<ItemStack> rightClickResult = stack.getItem().onItemRightClick(world, fakePlayer, Hand.MAIN_HAND);
                fakePlayer.setHeldItem(Hand.MAIN_HAND, rightClickResult.getResult());
            }
            if (fakePlayer.getHeldItem(Hand.MAIN_HAND).isEmpty()) {
                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(fakePlayer, copyBeforeUse, Hand.MAIN_HAND);
            }

            router.setBufferItemStack(fakePlayer.getHeldItemMainhand());
            return true;
        } else {
            return false;
        }
    }

    private boolean doActivateBlock(TileEntityItemRouter router, World world, PlayerEntity fakePlayer, float hitX, float hitY, float hitZ) {
        BlockPos targetPos = findBlockToActivate(router);
        if (targetPos == null) {
            return false;
        }
        Direction hitFace = getHitFace();

        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, Hand.MAIN_HAND, targetPos, hitFace);
        if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
            return false;
        }
        if (event.getUseBlock() != Event.Result.DENY) {
            BlockState iblockstate = world.getBlockState(targetPos);
            BlockRayTraceResult r = new BlockRayTraceResult(new Vec3d(hitX, hitY, hitZ), hitFace, targetPos, false);
            if (iblockstate.onBlockActivated(world, fakePlayer, Hand.MAIN_HAND, r) == ActionResultType.SUCCESS) {
                router.setBufferItemStack(fakePlayer.getHeldItemMainhand());
                return true;
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

    private Direction getHitFace() {
        switch (lookDirection) {
            case ABOVE:
                return Direction.DOWN;
            case BELOW:
                return Direction.UP;
            default:
                return getFacing().getOpposite();
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
