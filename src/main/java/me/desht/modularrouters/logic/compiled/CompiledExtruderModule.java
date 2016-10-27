package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.ExtruderModule;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CompiledExtruderModule extends CompiledModule {
    public static final String NBT_EXTRUDER_DIST = "ExtruderDist";

    private int distance;  // marks the current extension length (0 = no extrusion)

    public CompiledExtruderModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        distance = router.getExtData().getInteger(NBT_EXTRUDER_DIST + getFacing());
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.getRedstonePower() > 0 && !router.isBufferEmpty() && distance < ExtruderModule.maxDistance(router)) {
            // try to extend
            BlockPos nextPos = router.getPos().offset(getFacing(), distance + 1);
            ItemStack toPlace = router.peekBuffer(1);
            if (BlockUtil.tryPlaceAsBlock(toPlace, router.getWorld(), nextPos)) {
                router.extractBuffer(1);
                distance++;
                router.getExtData().setInteger(NBT_EXTRUDER_DIST + getFacing(), distance);
                if (Config.extruderSound) {
                    router.getWorld().playSound(null, nextPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        } else if (router.getRedstonePower() == 0 && !router.isBufferFull() && distance > 0) {
            // try to retract
            BlockPos pos = router.getPos().offset(getFacing(), distance);
            distance--;
            router.getExtData().setInteger(NBT_EXTRUDER_DIST + getFacing(), distance);
            List<ItemStack> drops = Lists.newArrayList();
            if (BlockUtil.tryBreakBlock(router.getWorld(), pos, getFilter(), drops, false, 0)) {
                for (ItemStack drop : drops) {
                    ItemStack excess = router.insertBuffer(drop);
                    if (excess != null) {
                        InventoryUtils.dropItems(router.getWorld(), pos, excess);
                    }
                }
                router.getWorld().playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                return true;
            }
        }
        return false;
    }
}
