package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ModuleAugmentSlot extends SlotItemHandler {
    private final TileEntityItemRouter router;
    private final EntityPlayer player;
    private final EnumHand hand;

    public ModuleAugmentSlot(AugmentHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.router = router;
        this.player = null;
        this.hand = null;
    }

    public ModuleAugmentSlot(AugmentHandler itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.player = player;
        this.hand = hand;
        this.router = null;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemAugment && isValidAugment(stack) && super.isItemValid(stack);
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();

        ((AugmentHandler)getItemHandler()).save();

        if (router != null && !router.getWorld().isRemote) {
            router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }

    private boolean isValidAugment(ItemStack stack) {
        ItemStack holderStack = ((AugmentHandler)getItemHandler()).getHolderStack();
        if (!(holderStack.getItem() instanceof ItemModule)) return false;
        ItemAugment augment = (ItemAugment) stack.getItem();
        return augment.isCompatible((ItemModule)holderStack.getItem());
    }
}

