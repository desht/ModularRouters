package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.SlotItemHandler;

public abstract class BaseModuleSlot<T extends BaseModuleHandler> extends SlotItemHandler {
    private final TileEntityItemRouter router;
    private final EntityPlayer player;
    private final EnumHand hand;
    private final boolean serverSide;
    private final int index;

    public BaseModuleSlot(T itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.router = router;
        this.player = null;
        this.hand = null;
        serverSide = !router.getWorld().isRemote;
        this.index = index;
    }

    public BaseModuleSlot(T itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.router = null;
        this.player = player;
        this.hand = hand;
        serverSide = !player.getEntityWorld().isRemote;
        this.index = index;
    }

    @Override
    public void putStack(ItemStack stack) {
        // bit of a hack, but ensures bulk item filter NBT is properly init'd
        if (stack.getItem() instanceof ItemSmartFilter) {
            NBTTagCompound compound = stack.getTag();
            if (compound == null || !compound.contains(ModuleHelper.NBT_FILTER)) {
                compound = new NBTTagCompound();
                compound.put(ModuleHelper.NBT_FILTER, new NBTTagList());
                stack.setTag(compound);
            }
        }

        // avoid saving the filter handler unnecessarily
        T handler = (T) getItemHandler();
        if (!ItemStack.areItemStacksEqual(stack, handler.getStackInSlot(index))) {
            handler.setStackInSlot(index, stack);
            onSlotChanged();
        }
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();

        T handler = (T) getItemHandler();
        handler.save();

        if (player != null && hand != null) {
            player.setHeldItem(hand, handler.getHolderStack());
        } else if (router != null && serverSide) {
            router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }

    public static class ModuleFilterSlot extends BaseModuleSlot<ModuleFilterHandler> {
        public ModuleFilterSlot(ModuleFilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(itemHandler, router, index, xPosition, yPosition);
        }

        public ModuleFilterSlot(ModuleFilterHandler itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
            super(itemHandler, player, hand, index, xPosition, yPosition);
        }
    }

    public static class BulkFilterSlot extends BaseModuleSlot<BulkFilterHandler> {
        public BulkFilterSlot(BulkFilterHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(itemHandler, router, index, xPosition, yPosition);
        }

        public BulkFilterSlot(BulkFilterHandler itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
            super(itemHandler, player, hand, index, xPosition, yPosition);
        }
    }
}
