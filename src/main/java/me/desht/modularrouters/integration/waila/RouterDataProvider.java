package me.desht.modularrouters.integration.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.List;

@Optional.Interface(modid = "Waila", iface = "mcp.mobius.waila.api.IWailaDataProvider")
public class RouterDataProvider implements IWailaDataProvider {
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();
        if (te instanceof TileEntityItemRouter) {
            TileEntityItemRouter router = (TileEntityItemRouter) te;
            if (router.isPermitted(accessor.getPlayer())) {
                MiscUtil.appendMultiline(currenttip, "itemText.misc.moduleCount", router.getModuleCount());
                for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
                    if (router.getUpgradeCount(type) > 0) {
                        String name = MiscUtil.translate("item." + type.toString().toLowerCase() + "Upgrade.name");
                        currenttip.add(MiscUtil.translate("itemText.misc.upgradeCount", name, router.getUpgradeCount(type)));
                    }
                }
                currenttip.add(TextFormatting.RED + MiscUtil.translate("guiText.tooltip.redstone." + router.getRedstoneBehaviour()));
            } else {
                currenttip.add(MiscUtil.translate("chatText.security.accessDenied"));
            }
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        return tag;
    }
}
