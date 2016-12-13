package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.FilterHandler;
import me.desht.modularrouters.container.FilterHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

/**
 * Sent when a filter's settings have been changed in any way via its GUI.
 * The filter could be in a player's hand, or in a module (which may or may not be in a router...)
 */
public class FilterSettingsMessage extends BaseSettingsMessage {
    private int filterIndex;
    private Operation op;

    public enum Operation {
        CLEAR_ALL, REMOVE_ITEM, MERGE, LOAD, ADD_STRING, REMOVE_AT, ANY_ALL_FLAG;
    }

    public FilterSettingsMessage() {}

    public FilterSettingsMessage(Operation op, EnumHand hand, int filterIndex, NBTTagCompound ext) {
        this(op, null, -1, filterIndex, hand, ext);
    }

    public FilterSettingsMessage(Operation op, BlockPos routerPos, int moduleIndex, int filterIndex, NBTTagCompound ext) {
        this(op, routerPos, moduleIndex, filterIndex, null, ext);
    }

    private FilterSettingsMessage(Operation op, BlockPos routerPos, int moduleIndex, int filterIndex, EnumHand hand, NBTTagCompound ext) {
        super(routerPos, hand, moduleIndex, ext);
        this.op = op;
        this.filterIndex = filterIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        op = Operation.values()[buf.readByte()];
        filterIndex = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(op.ordinal());
        buf.writeByte(filterIndex);
    }

    public Operation getOp() {
        return op;
    }

    public IItemHandler getTargetInventory() {
        ModuleTarget target = ModuleTarget.fromNBT(getNbtData());
        World w = DimensionManager.getWorld(target.dimId);
        if (w != null) {
            return InventoryUtils.getInventory(w, target.pos, target.face);
        }
        return null;
    }

    public static class Handler implements IMessageHandler<FilterSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(FilterSettingsMessage message, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.getEntityWorld();
            mainThread.addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                ItemStack filterStack = ItemStack.EMPTY;
                ItemStack moduleStack = ItemStack.EMPTY;
                FilterHandler filterHandler = null;
                if (message.routerPos != null) {
                    TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.getEntityWorld(), message.routerPos);
                    if (router != null) {
                        moduleStack = router.getModules().getStackInSlot(message.moduleSlotIndex);
                        filterHandler = new ModuleFilterHandler(moduleStack);
                        filterStack = filterHandler.getStackInSlot(message.filterIndex);
                        router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
                    }
                } else if (message.hand != null) {
                    ItemStack heldStack = player.getHeldItem(message.hand);
                    if (ItemModule.getModule(heldStack) != null) {
                        moduleStack = heldStack;
                        filterHandler = new ModuleFilterHandler(moduleStack);
                        filterStack = filterHandler.getStackInSlot(message.filterIndex);
                    } else if (ItemSmartFilter.getFilter(heldStack) != null) {
                        filterStack = heldStack;
                    }
                }
                SmartFilter sf = ItemSmartFilter.getFilter(filterStack);
                if (sf != null) {
                    IMessage response = sf.dispatchMessage(player, message, filterStack, moduleStack);
                    if (filterHandler != null) {
                        filterHandler.setStackInSlot(message.filterIndex, filterStack);
                        filterHandler.save();
                        if (message.hand != null) {
                            player.setHeldItem(message.hand, filterHandler.getHoldingItemStack());
                        }
                    }
                    if (response != null) {
                        // send to any nearby players in case they also have the GUI open
                        NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 8);
                        ModularRouters.network.sendToAllAround(response, tp);
                    }
                }
            });
            return null;
        }
    }
}
