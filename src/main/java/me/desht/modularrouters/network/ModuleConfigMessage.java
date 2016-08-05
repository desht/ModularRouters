package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used when a player wants to configure a module that is already installed
 * in an item router.
 */
public class ModuleConfigMessage implements IMessage {
    private BlockPos routerPos;
    private int slotIndex;

    public ModuleConfigMessage() {
    }

    public ModuleConfigMessage(BlockPos routerPos, int slotIndex) {
        this.routerPos = routerPos;
        this.slotIndex = slotIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        routerPos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        slotIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(routerPos.getX());
        buf.writeInt(routerPos.getY());
        buf.writeInt(routerPos.getZ());
        buf.writeInt(slotIndex);
    }

    public static class Handler implements IMessageHandler<ModuleConfigMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleConfigMessage message, MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
            mainThread.addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                BlockPos pos = message.routerPos;
                TileEntity te = player.getEntityWorld().getTileEntity(pos);
                if (te instanceof TileEntityItemRouter) {
                    TileEntityItemRouter router = (TileEntityItemRouter) te;
                    router.playerConfiguringModule(player, message.slotIndex);
                    player.openGui(ModularRouters.instance, ModularRouters.GUI_MODULE_INSTALLED,
                            player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ());
                }
            });
            return null;
        }
    }
}
