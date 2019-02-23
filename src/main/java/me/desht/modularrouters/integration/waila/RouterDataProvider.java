package me.desht.modularrouters.integration.waila;

// todo 1.13
//@Optional.Interface(modid = "waila", iface = "mcp.mobius.waila.api.IWailaDataProvider")
public class RouterDataProvider /*implements IWailaDataProvider*/ {
//    @Override
//    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        return null;
//    }
//
//    @Override
//    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        return currenttip;
//    }
//
//    @Override
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        TileEntity te = accessor.getTileEntity();
//        if (te instanceof TileEntityItemRouter) {
//            TileEntityItemRouter router = (TileEntityItemRouter) te;
//            if (router.isPermitted(accessor.getPlayer())) {
//                MiscUtil.appendMultiline(currenttip, "itemText.misc.moduleCount", router.getModuleCount());
//                for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
//                    if (router.getUpgradeCount(type) > 0) {
//                        String name = MiscUtil.translate("item." + type.toString().toLowerCase() + "_upgrade.name");
//                        currenttip.add(MiscUtil.translate("itemText.misc.upgradeCount", name, router.getUpgradeCount(type)));
//                    }
//                }
//                currenttip.add(TextFormatting.WHITE + MiscUtil.translate("guiText.tooltip.redstone.label")
//                        + ": " + TextFormatting.AQUA + MiscUtil.translate("guiText.tooltip.redstone." + router.getRedstoneBehaviour()));
//            } else {
//                currenttip.add(MiscUtil.translate("chatText.security.accessDenied"));
//            }
//        }
//
//        return currenttip;
//    }
//
//    @Override
//    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        return currenttip;
//    }
//
//    @Override
//    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
//        return tag;
//    }
}
