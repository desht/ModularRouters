package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.Sender2Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemSenderModule2 extends TargetedSender {
    public ItemSenderModule2() {
        super("senderModule2");
    }

    ItemSenderModule2(String name) {
        super(name);
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new Sender2Executor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        DimensionPos dpos = getTarget(itemstack);
        if (dpos != null) {
            WorldServer w = DimensionManager.getWorld(dpos.dimId);
            if (w != null) {
                list.add(I18n.translateToLocalFormatted("itemText.misc.target", dpos.dimId, dpos.pos.getX(), dpos.pos.getY(), dpos.pos.getZ(), dpos.face.getName()));
            }
            if (isRangeLimited() && Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
                TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).tileEntityItemRouter;
                if (router != null && dpos.pos.distanceSq(router.getPos()) > maxDistanceSq(router)) {
                    list.add(I18n.translateToLocal("itemText.misc.outOfRange"));
                }
            }
        }
        super.addInformation(itemstack, player, list, par4);
    }

    protected boolean isRangeLimited() {
        return true;
    }

    public static int maxDistanceSq(TileEntityItemRouter router) {
        // TODO precalculate to avoid repeated multiplications
        int r = Config.sender2BaseRange + (Math.min(router.getRangeUpgrades(), Config.sender2BaseRange));
        return r * r;
    }
}
