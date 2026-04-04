package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.ModularRoutersTags;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.TranslatableEnum;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public class CompiledActivatorModule extends CompiledModule {
    private final ActivatorSettings settings;
    private int entityIdx;

    private final Set<String> BLOCK_METHODS = ImmutableSet.of(
            "onBlockActivated", "use"
    );
    private final Set<String> ITEM_METHODS = ImmutableSet.of(
            "onItemUseFirst", // forge method, no SRG name
            "onItemUse", "useOn",
            "onItemRightClick", "use"
    );
    private static final Set<Item> itemBlacklist = new HashSet<>();
    private static final Set<Block> blockBlacklist = new HashSet<>();

    public CompiledActivatorModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.ACTIVATOR_SETTINGS, ActivatorSettings.DEFAULT);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        ItemStack stack = router.getBufferItemStack();

        if (stack.is(ModularRoutersTags.Items.ACTIVATOR_BLACKLIST) || itemBlacklist.contains(stack.getItem())) {
            return false;
        }

        // we'll allow an empty stack, since right-clicking with an empty hand is a valid operation
        // - but only if there's an empty or blacklist filter
        if (!stack.isEmpty() && !getFilter().test(stack) || stack.isEmpty() && !getFilter().isEmpty() && getFilter().isWhiteList()) {
            return false;
        }

        RouterFakePlayer fakePlayer = router.getFakePlayer();
        Vec3 centre = Vec3.atCenterOf(router.getBlockPos());
        // place the fake player just outside the router, on the correct face
        fakePlayer.setPos(centre.x() + getAbsoluteFacing().getStepX() * 0.501, centre.y() + getAbsoluteFacing().getStepY() * 0.501, centre.z() + getAbsoluteFacing().getStepZ() * 0.501);
        fakePlayer.setShiftKeyDown(settings.sneaking);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);

        boolean didWork = switch (settings.actionType) {
            case ITEM_OR_BLOCK -> doUseItem(router, fakePlayer);
            case USE_ITEM_ON_ENTITY -> doUseItemOnEntity(router, fakePlayer);
            case ATTACK_ENTITY -> doAttackEntity(router, fakePlayer);
        };

        if (didWork) {
            router.setBufferItemStack(fakePlayer.getMainHandItem());
            dropExtraItems(router, fakePlayer);
        }

        return didWork;
    }

    private boolean doUseItem(ModularRouterBlockEntity router, FakePlayer fakePlayer) {
        BlockPos pos = router.getBlockPos();
        Level world = Objects.requireNonNull(router.getLevel());
        ItemStack stack = router.getBufferItemStack();
        fakePlayer.setYRot(MiscUtil.getYawFromFacing(getAbsoluteFacing()));
        fakePlayer.setXRot(getAbsoluteFacing().getAxis() == Direction.Axis.Y ? getAbsoluteFacing().getStepY() * -90 : settings.lookDirection.pitch);
        BlockHitResult hitResult = doRayTrace(pos, fakePlayer);
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        if (hitResult.getType() != HitResult.Type.MISS && blockBlacklist.contains(state.getBlock())) {
            return false;
        }
        try {
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);
            boolean result = fakePlayer.gameMode.useItemOn(fakePlayer, world, stack, InteractionHand.MAIN_HAND, hitResult).consumesAction()
                    || fakePlayer.gameMode.useItem(fakePlayer, world, stack, InteractionHand.MAIN_HAND).consumesAction();
            router.setBufferItemStack(fakePlayer.getMainHandItem()); // in case using the item modified it
            return result;
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

        int xOff = getAbsoluteFacing().getStepX();
        int yOff = getAbsoluteFacing().getStepY();
        int zOff = getAbsoluteFacing().getStepZ();

        BlockPos.MutableBlockPos targetPos = routerPos.relative(getAbsoluteFacing()).mutable();
        LookDirection lookDirection = settings.lookDirection;
        if (lookDirection != LookDirection.LEVEL
                && Block.isShapeFullBlock(fp.level().getBlockState(targetPos).getShape(fp.level(), targetPos)))
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

        double reachDist = getRangeSquared();
        for (; targetPos.distSqr(routerPos) <= reachDist; targetPos.move(xOff, yOff, zOff)) {
            if (fp.level().isEmptyBlock(targetPos)) {
                continue;
            }
            VoxelShape shape = fp.level().getBlockState(targetPos).getShape(fp.level(), targetPos);
            if (shape.isEmpty()) {
                continue;
            }
            Vec3 targetVec = shape.toAabbs().getFirst().getCenter().add(Vec3.atLowerCornerOf(targetPos));
            BlockHitResult res = fp.level().clip(
                    new ClipContext(fpVec, targetVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, fp)
            );
            if (res.getType() == HitResult.Type.BLOCK) {
                return res;
            }
        }

        return BlockHitResult.miss(fpVec.add(fp.getLookAngle()), getAbsoluteFacing().getOpposite(), routerPos.relative(getAbsoluteFacing()));
    }

    private boolean doAttackEntity(ModularRouterBlockEntity router, RouterFakePlayer fakePlayer) {
        LivingEntity entity = findEntity(router, LivingEntity.class, this::passesAttackBlacklist);
        if (entity == null || entity instanceof Player p && router.getUpgradeCount(ModItems.SECURITY_UPGRADE.get()) > 0 && router.isPermitted(p)) {
            return false;
        }
        fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, entity.position());
        fakePlayer.attack(entity);
        return true;
    }

    private boolean doUseItemOnEntity(ModularRouterBlockEntity router, FakePlayer fakePlayer) {
        Entity entity = findEntity(router, Entity.class, this::passesUseBlacklist);
        if (entity == null) {
            return false;
        }
        InteractionResult result = fakePlayer.interactOn(entity, InteractionHand.MAIN_HAND, entity.getEyePosition());
        if (result.consumesAction()) {
            router.setBufferItemStack(fakePlayer.getMainHandItem());
            return true;
        }
        return false;
    }

    private <T extends Entity> T findEntity(ModularRouterBlockEntity router, Class<T> cls, Predicate<Entity> blacklistChecker) {
        final Direction face = Objects.requireNonNull(getAbsoluteFacing());
        final BlockPos pos = router.getBlockPos();
        final Vec3 vec = Vec3.atCenterOf(pos);
        final double expand = getRange() / 2.0;
        final double moveBy = expand + 0.5;
        final AABB box = new AABB(vec, vec)
                .move(face.getStepX() * moveBy, face.getStepY() * moveBy, face.getStepZ() * moveBy)
                .inflate(expand);
        final List<T> l = Objects.requireNonNull(router.getLevel()).getEntitiesOfClass(cls, box, blacklistChecker);
        if (l.isEmpty()) {
            return null;
        }

        switch (settings.entityMode) {
            case RANDOM:
                return l.get(router.getLevel().getRandom().nextInt(l.size()));
            case NEAREST:
                l.sort(Comparator.comparingDouble(o -> o.distanceToSqr(vec)));
                return l.getFirst();
            case ROUND_ROBIN:
                l.sort(Comparator.comparingDouble(o -> o.distanceToSqr(vec)));
                entityIdx = (entityIdx + 1) % l.size();
                return l.get(entityIdx);
            default:
                return null;
        }
    }

    private boolean passesAttackBlacklist(Entity e) {
        return !e.is(ModularRoutersTags.EntityTypes.activatorAttackBlacklist);
    }

    private boolean passesUseBlacklist(Entity e) {
        return !e.is(ModularRoutersTags.EntityTypes.activatorInteractBlacklist);
    }

    private void dropExtraItems(ModularRouterBlockEntity router, Player fakePlayer) {
        // any items added to the fake player's inventory from using the held item need to be dropped into
        // the world, since the router has no access to them, and the player would otherwise lose them
        // e.g. milking a cow with a stack of buckets in the router slot
        NonNullList<ItemStack> inv = fakePlayer.getInventory().getNonEquipmentItems();
        Vec3 where = Vec3.atCenterOf(router.getBlockPos().relative(getAbsoluteFacing()));
        // start at slot 1, since slot 0 is always used for the fake player's held item, which doesn't get dropped
        Level level = Objects.requireNonNull(router.getLevel());
        for (int i = 1; i < inv.size() && !inv.get(i).isEmpty(); i++) {
            ItemEntity item = new ItemEntity(level, where.x(), where.y(), where.z(), inv.get(i));
            router.getLevel().addFreshEntity(item);
            inv.set(i, ItemStack.EMPTY);
        }
    }

    public ActionType getActionType() {
        return settings.actionType;
    }

    public LookDirection getLookDirection() {
        return settings.lookDirection;
    }

    public EntityMode getEntityMode() {
        return settings.entityMode;
    }

    public boolean isSneaking() {
        return settings.sneaking;
    }

    @Override
    public int getEnergyCost() {
        return settings.actionType == ActionType.ATTACK_ENTITY ?
                ConfigHolder.common.energyCosts.activatorModuleEnergyCostAttack.get() :
                ConfigHolder.common.energyCosts.activatorModuleEnergyCost.get();
    }

    @Override
    public boolean careAboutItemAttributes() {
        return settings.actionType == ActionType.ATTACK_ENTITY;
    }

    public enum ActionType implements TranslatableEnum, StringRepresentable {
        ITEM_OR_BLOCK("item_or_block", false),
        USE_ITEM_ON_ENTITY("use_item_on_entity", true),
        ATTACK_ENTITY("attack_entity", true);

        private final boolean entity;
        private final String name;

        ActionType(String name, boolean entity) {
            this.entity = entity;
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.activator.action." + name;
        }

        public boolean isEntityTarget() {
            return entity;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum LookDirection implements TranslatableEnum, StringRepresentable {
        LEVEL("level", 0f),
        ABOVE("above", -45f),
        BELOW("below", 45f);

        private final String name;
        private final float pitch;

        LookDirection(String name, float pitch) {
            this.name = name;
            this.pitch = pitch;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.activator.direction." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum EntityMode implements TranslatableEnum, StringRepresentable {
        NEAREST("nearest"),
        RANDOM("random"),
        ROUND_ROBIN("round_robin");

        private final String name;

        EntityMode(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.itemText.activator.entityMode." + getSerializedName();
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record ActivatorSettings(ActionType actionType, LookDirection lookDirection, EntityMode entityMode, boolean sneaking)
    {
        public static final ActivatorSettings DEFAULT = new ActivatorSettings(
                ActionType.ITEM_OR_BLOCK, LookDirection.LEVEL, EntityMode.NEAREST, false
        );

        public static final Codec<ActivatorSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                StringRepresentable.fromEnum(ActionType::values)
                        .optionalFieldOf("action", ActionType.USE_ITEM_ON_ENTITY)
                        .forGetter(ActivatorSettings::actionType),
                StringRepresentable.fromEnum(LookDirection::values)
                        .optionalFieldOf("look_direction", LookDirection.LEVEL)
                        .forGetter(ActivatorSettings::lookDirection),
                StringRepresentable.fromEnum(EntityMode::values)
                        .optionalFieldOf("entity_mode", EntityMode.NEAREST)
                        .forGetter(ActivatorSettings::entityMode),
                Codec.BOOL
                        .optionalFieldOf("sneaking", false)
                        .forGetter(ActivatorSettings::sneaking)
        ).apply(builder, ActivatorSettings::new));

        public static final StreamCodec<FriendlyByteBuf,ActivatorSettings> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(ActionType.class), ActivatorSettings::actionType,
                NeoForgeStreamCodecs.enumCodec(LookDirection.class), ActivatorSettings::lookDirection,
                NeoForgeStreamCodecs.enumCodec(EntityMode.class), ActivatorSettings::entityMode,
                ByteBufCodecs.BOOL, ActivatorSettings::sneaking,
                ActivatorSettings::new
        );
    }
}
