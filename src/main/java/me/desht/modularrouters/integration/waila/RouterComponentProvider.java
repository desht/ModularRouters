package me.desht.modularrouters.integration.waila;

public class RouterComponentProvider /*implements IComponentProvider*/ {
//    @Override
//    public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
//        CompoundTag data = accessor.getServerData();
//        if (accessor.getTileEntity() instanceof ModularRouterBlockEntity) {
//            if (data.getBoolean("Denied")) {
//                tooltip.add(xlate("modularrouters.chatText.security.accessDenied"));
//            } else {
//                if (data.getInt("ModuleCount") > 0) {
//                    MiscUtil.appendMultilineText(tooltip, ChatFormatting.WHITE, "modularrouters.itemText.misc.moduleCount", data.getInt("ModuleCount"));
//                }
//                CompoundTag upgrades = data.getCompound("Upgrades");
//                if (!upgrades.isEmpty()) {
//                    tooltip.add(xlate("modularrouters.itemText.misc.upgrades"));
//                    for (String k : upgrades.getAllKeys()) {
//                        tooltip.add(xlate("modularrouters.itemText.misc.upgradeCount", upgrades.getInt(k), I18n.get(k)));
//                    }
//                }
//                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
//                tooltip.add(xlate("modularrouters.guiText.tooltip.redstone.label")
//                        .append(": " + ChatFormatting.AQUA)
//                        .append(xlate("modularrouters.guiText.tooltip.redstone." + rrb))
//                );
//                if (data.getBoolean("EcoMode")) {
//                    tooltip.add(xlate("modularrouters.itemText.misc.ecoMode"));
//                }
//            }
//        }
//    }
}
