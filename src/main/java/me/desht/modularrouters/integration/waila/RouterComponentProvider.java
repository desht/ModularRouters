package me.desht.modularrouters.integration.waila;

public class RouterComponentProvider /*implements IBlockComponentProvider*/ {
//    @Override
//    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
//        CompoundTag data = blockAccessor.getServerData();
//        if (blockAccessor.getBlockEntity() instanceof ModularRouterBlockEntity) {
//            if (data.getBoolean("Denied")) {
//                iTooltip.add(xlate("modularrouters.chatText.security.accessDenied"));
//            } else {
//                if (data.getInt("ModuleCount") > 0) {
//                    List<Component> componentList = new ArrayList<>();
//                    MiscUtil.appendMultilineText(componentList, ChatFormatting.WHITE, "modularrouters.itemText.misc.moduleCount", data.getInt("ModuleCount"));
//                    iTooltip.addAll(componentList);
//                }
//                CompoundTag upgrades = data.getCompound("Upgrades");
//                if (!upgrades.isEmpty()) {
//                    iTooltip.add(xlate("modularrouters.itemText.misc.upgrades"));
//                    for (String k : upgrades.getAllKeys()) {
//                        iTooltip.add(xlate("modularrouters.itemText.misc.upgradeCount", upgrades.getInt(k), I18n.get(k)));
//                    }
//                }
//                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
//                iTooltip.add(xlate("modularrouters.guiText.tooltip.redstone.label")
//                        .append(": " + ChatFormatting.AQUA)
//                        .append(xlate("modularrouters.guiText.tooltip.redstone." + rrb))
//                );
//                if (data.getBoolean("EcoMode")) {
//                    iTooltip.add(xlate("modularrouters.itemText.misc.ecoMode"));
//                }
//            }
//        }
//    }
//
//    @Override
//    public ResourceLocation getUid() {
//        return RL("router");
//    }

}
