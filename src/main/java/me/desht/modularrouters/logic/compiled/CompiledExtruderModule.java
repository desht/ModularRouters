package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.ExtruderModule;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class CompiledExtruderModule extends CompiledModule {
    public static final String NBT_EXTRUDER_DIST = "ExtruderDist";

    private final boolean silkTouch;
    private int distance;  // marks the current extension length (0 = no extrusion)

    public CompiledExtruderModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        distance = router.getExtData().getInteger(NBT_EXTRUDER_DIST + getFacing());
        silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        boolean extend = shouldExtend(router);
        World world = router.getWorld();

        if (extend && !router.isBufferEmpty() && distance < ExtruderModule.maxDistance(router) && isRegulationOK(router, false)) {
            // try to extend
            BlockPos placePos = router.getPos().offset(getFacing(), distance + 1);
            ItemStack toPlace = router.peekBuffer(1);
            IBlockState state = BlockUtil.tryPlaceAsBlock(toPlace, world, placePos);
            if (state != null) {
                router.extractBuffer(1);
                distance++;
                router.getExtData().setInteger(NBT_EXTRUDER_DIST + getFacing(), distance);
                if (Config.extruderSound) {
                    router.playSound(null, placePos,
                            state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        } else if (!extend && !router.isBufferFull() && distance > 0 && isRegulationOK(router, true)) {
            // try to retract
            BlockPos breakPos = router.getPos().offset(getFacing(), distance);
            List<ItemStack> drops = Lists.newArrayList();
            IBlockState oldState = world.getBlockState(breakPos);
            if (BlockUtil.tryBreakBlock(world, breakPos, getFilter(), drops, silkTouch, 0)) {
                distance--;
                router.getExtData().setInteger(NBT_EXTRUDER_DIST + getFacing(), distance);
                for (ItemStack drop : drops) {
                    ItemStack excess = router.insertBuffer(drop);
                    if (!excess.isEmpty()) {
                        InventoryUtils.dropItems(world, breakPos, excess);
                    }
                }
                if (Config.extruderSound) {
                    router.playSound(null, breakPos,
                            oldState.getBlock().getSoundType(oldState, world, breakPos, null).getBreakSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRun(boolean powered, boolean pulsed) {
        return true;
    }

    private boolean shouldExtend(TileEntityItemRouter router) {
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
