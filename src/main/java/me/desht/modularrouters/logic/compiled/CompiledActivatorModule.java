package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.FakePlayerManager;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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

    private final ActionType actionType;
    private final LookDirection lookDirection;
    private final boolean sneaking;

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

    public CompiledActivatorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = stack.getTag();
        if (compound != null) {
            actionType = ActionType.values()[compound.getInt(NBT_ACTION_TYPE)];
            lookDirection = LookDirection.values()[compound.getInt(NBT_LOOK_DIRECTION)];
            sneaking = compound.getBoolean(NBT_SNEAKING);
        } else {
            actionType = ActionType.ACTIVATE_BLOCK;
            lookDirection = LookDirection.LEVEL;
            sneaking = false;
        }
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        World world = router.getWorld();
        BlockPos pos = router.getPos();

        FakePlayer fakePlayer = FakePlayerManager.getFakePlayer((WorldServer) world, pos);
        if (fakePlayer == null) {
            return false;
        }
        fakePlayer.setPosition(pos.getX() + 0.5, pos.getY() + 0.5 - fakePlayer.getEyeHeight(), pos.getZ() + 0.5);
        fakePlayer.rotationPitch = getFacing().getYOffset() * -90;
        fakePlayer.rotationYaw = MiscUtil.getYawFromFacing(getFacing());
        fakePlayer.setSneaking(sneaking);
        ItemStack stack = router.getBufferItemStack();
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, stack);
        float hitX = (float)(fakePlayer.posX - pos.getX());
        float hitY = (float)(fakePlayer.posY - pos.getY());
        float hitZ = (float)(fakePlayer.posZ - pos.getZ());

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
        Entity entity = findNearestEntity(router);
        if (entity == null) {
            return false;
        }
        EnumActionResult result = fakePlayer.interactOn(entity, EnumHand.MAIN_HAND);
        if (result == EnumActionResult.SUCCESS) {
            router.setBufferItemStack(fakePlayer.getHeldItemMainhand());
            return true;
        }
        return false;
    }

    private Entity findNearestEntity(TileEntityItemRouter router) {
        EnumFacing face = getFacing();
        final BlockPos pos = router.getPos();
        AxisAlignedBB box = new AxisAlignedBB(pos.offset(face))
                .expand(face.getXOffset() * 3, face.getYOffset() * 3, face.getZOffset() * 3);

        List<Entity> l = router.getWorld().getEntitiesWithinAABB(Entity.class, box);
        if (l.isEmpty()) {
            return null;
        }
        l.sort(Comparator.comparingDouble(o -> o.getDistanceSq(pos)));
        return l.get(0);
    }

    private boolean doUseItem(TileEntityItemRouter router, World world, BlockPos pos, EntityPlayer fakePlayer, float hitX, float hitY, float hitZ) {
        BlockPos targetPos = pos.offset(getFacing());
        EnumFacing hitFace = getHitFace();
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
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, EnumHand.MAIN_HAND, targetPos, hitFace,  ForgeHooks.rayTraceEyeHitVec(fakePlayer, 2.0D));
        if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
            return false;
        }

        EnumActionResult ret = stack.onItemUseFirst(new ItemUseContext(fakePlayer, stack, targetPos, hitFace, hitX, hitY, hitZ));
        if (ret != EnumActionResult.PASS) return false;

        if (stack.isEmpty() || fakePlayer.getCooldownTracker().hasCooldown(stack.getItem())) {
            return false;
        }

        EnumActionResult result;

        if (stack.getItem() instanceof ItemBlock && !fakePlayer.canUseCommandBlock()) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                return false;
            }
        }

        if (event.getUseItem() != Event.Result.DENY) {
            ItemStack copyBeforeUse = stack.copy();
            result = stack.onItemUse(new ItemUseContext(fakePlayer, stack, targetPos, hitFace, hitX, hitY, hitZ));
            if (result == EnumActionResult.PASS) {
                ActionResult<ItemStack> rightClickResult = stack.getItem().onItemRightClick(world, fakePlayer, EnumHand.MAIN_HAND);
                fakePlayer.setHeldItem(EnumHand.MAIN_HAND, rightClickResult.getResult());
            }
            if (fakePlayer.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(fakePlayer, copyBeforeUse, EnumHand.MAIN_HAND);
            }

            router.setBufferItemStack(fakePlayer.getHeldItemMainhand());
            return true;
        } else {
            return false;
        }
    }

    private boolean doActivateBlock(TileEntityItemRouter router, World world, EntityPlayer fakePlayer, float hitX, float hitY, float hitZ) {
        BlockPos targetPos = findBlockToActivate(router);
        if (targetPos == null) {
            return false;
        }
        EnumFacing hitFace = getHitFace();

        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(fakePlayer, EnumHand.MAIN_HAND, targetPos, hitFace,  ForgeHooks.rayTraceEyeHitVec(fakePlayer, 2.0D));
        if (event.isCanceled() || event.getUseItem() == Event.Result.DENY) {
            return false;
        }
        if (event.getUseBlock() != Event.Result.DENY) {
            IBlockState iblockstate = world.getBlockState(targetPos);
            if (iblockstate.onBlockActivated(world, targetPos, fakePlayer, EnumHand.MAIN_HAND, hitFace, hitX, hitY, hitZ)) {
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

    private EnumFacing getHitFace() {
        switch (lookDirection) {
            case ABOVE:
                return EnumFacing.DOWN;
            case BELOW:
                return EnumFacing.UP;
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

    public boolean isSneaking() {
        return sneaking;
    }

}
