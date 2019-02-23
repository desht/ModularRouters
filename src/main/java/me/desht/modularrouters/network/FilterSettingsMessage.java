package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent when a filter's settings have been changed in any way via its GUI.
 * The filter could be in a player's hand, or in a module (which may or may not be in a router...)
 */
public class FilterSettingsMessage extends BaseSettingsMessage {
    private Operation op;

    public enum Operation {
        CLEAR_ALL, REMOVE_ITEM, MERGE, LOAD, ADD_STRING, REMOVE_AT, ANY_ALL_FLAG
    }

    public FilterSettingsMessage() {}

    public FilterSettingsMessage(Operation op, EnumHand hand, NBTTagCompound ext) {
        this(op, null, hand, ext);
    }

    public FilterSettingsMessage(Operation op, BlockPos routerPos, NBTTagCompound ext) {
        this(op, routerPos, null, ext);
    }

    private FilterSettingsMessage(Operation op, BlockPos routerPos, EnumHand hand, NBTTagCompound ext) {
        super(routerPos, hand, ext);
        this.op = op;
    }

    public FilterSettingsMessage(ByteBuf buf) {
        super(buf);
        op = Operation.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(op.ordinal());
    }

    public Operation getOp() {
        return op;
    }

    public IItemHandler getTargetInventory() {
        ModuleTarget target = ModuleTarget.fromNBT(getNbtData());
        World w = MiscUtil.getWorldForDimensionId(target.dimId);
        return w != null ? InventoryUtils.getInventory(w, target.pos, target.face) : null;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EntityPlayerMP player = ctx.get().getSender();
            ItemStack filterStack = ItemStack.EMPTY;
            ItemStack moduleStack = ItemStack.EMPTY;
            ModuleFilterHandler filterHandler = null;
            SlotTracker tracker = SlotTracker.getInstance(player);
            if (routerPos != null) {
                TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.getEntityWorld(), routerPos);
                if (router != null) {
                    moduleStack = tracker.getConfiguringModule(router);
                    filterStack = tracker.getConfiguringFilter(router);
                    router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
                }
            } else if (hand != null) {
                ItemStack heldStack = player.getHeldItem(hand);
                if (heldStack.getItem() instanceof ItemModule) {
                    moduleStack = heldStack;
                    filterHandler = new ModuleFilterHandler(moduleStack);
                    filterStack = filterHandler.getStackInSlot(tracker.getFilterSlot());
                } else if (heldStack.getItem() instanceof ItemSmartFilter) {
                    filterStack = heldStack;
                }
            }
            if (filterStack.getItem() instanceof ItemSmartFilter) {
                ItemSmartFilter sf = (ItemSmartFilter) filterStack.getItem();
                GuiSyncMessage response = sf.dispatchMessage(player, this, filterStack, moduleStack);
                if (filterHandler != null) {
                    filterHandler.setStackInSlot(tracker.getFilterSlot(), filterStack);
                    filterHandler.save();
                    if (hand != null) {
                        player.setHeldItem(hand, filterHandler.getHolderStack());
                    }
                }
                if (response != null) {
                    // send to any nearby players in case they also have the GUI open
                    PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(player.posX, player.posY, player.posZ,
                            8, player.getEntityWorld().getDimension().getType());
                    PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp), response);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
