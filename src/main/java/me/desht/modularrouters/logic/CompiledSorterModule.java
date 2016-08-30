package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

public class CompiledSorterModule extends CompiledModule {
    private int lastMatchPos = 0;

    public CompiledSorterModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    public int getLastMatchPos() {
        return lastMatchPos;
    }

    public void setLastMatchPos(int lastMatchPos) {
        this.lastMatchPos = lastMatchPos;
    }
}
