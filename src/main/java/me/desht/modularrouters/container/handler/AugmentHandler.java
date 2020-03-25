package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class AugmentHandler extends ItemStackHandler {
    private final ItemStack holderStack;
    private final TileEntityItemRouter router;

    public AugmentHandler(ItemStack holderStack, TileEntityItemRouter router) {
        super(ItemAugment.SLOTS);
        this.router = router;

        Validate.isTrue(holderStack.getItem() instanceof ItemModule, "holder stack must be a module!");

        this.holderStack = holderStack;
        deserializeNBT(holderStack.getOrCreateChildTag(ModularRouters.MODID).getCompound(ModuleHelper.NBT_AUGMENTS));
    }

    public ItemStack getHolderStack() {
        return holderStack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemAugment) {
            ItemAugment augment = (ItemAugment) stack.getItem();
            return augment.isCompatible((ItemModule) holderStack.getItem());
        }
        return false;
    }

    @Override
    protected void onContentsChanged(int slot) {
        save();
    }

    private void save() {
        holderStack.getOrCreateChildTag(ModularRouters.MODID).put(ModuleHelper.NBT_AUGMENTS, serializeNBT());
        if (router != null) {
            router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }
}
