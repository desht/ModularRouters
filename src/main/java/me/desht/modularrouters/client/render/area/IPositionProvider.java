package me.desht.modularrouters.client.render.area;

import me.desht.modularrouters.item.module.ITargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Represents an item which can store & provide one or more block positions.
 */
public interface IPositionProvider {
    /**
     * Get block position data from the given ItemStack.  It is up to the implementor to decide how the block positions
     * should be stored on the itemstack and in what order they should be returned.
     *
     * @param stack the itemstack
     * @return a list of block positions that has been retrieved from the itemstack
     */
    default List<ModuleTarget> getStoredPositions(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ITargetedModule) {
            return List.copyOf(ITargetedModule.getTargets(stack, false));
        }
        return List.of();
    }

    /**
     * Color that should be used to highlight the stored block positions if & when they are rendered on-screen.
     *
     * @param index the index in the list returned by getStoredPositions()
     * @return a color in ARGB format, or 0 to skip rendering completely
     */
    default int getRenderColor(int index) { return 0xFFFFFF00; }
}

