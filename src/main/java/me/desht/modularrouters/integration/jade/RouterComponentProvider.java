//package me.desht.modularrouters.integration.jade;
//
//import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
//import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
//import net.minecraft.ChatFormatting;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.Identifier;
//import snownee.jade.api.BlockAccessor;
//import snownee.jade.api.IBlockComponentProvider;
//import snownee.jade.api.ITooltip;
//import snownee.jade.api.config.IPluginConfig;
//
//import static me.desht.modularrouters.client.util.ClientUtil.xlate;
//import static me.desht.modularrouters.util.MiscUtil.RL;
//
//public class RouterComponentProvider implements IBlockComponentProvider {
//    @Override
//    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
//        CompoundTag data = blockAccessor.getServerData();
//        if (blockAccessor.getBlockEntity() instanceof ModularRouterBlockEntity) {
//            if (data.getBooleanOr("Denied", false)) {
//                iTooltip.add(xlate("modularrouters.chatText.security.accessDenied").withStyle(ChatFormatting.RED));
//            } else {
//                data.getInt("ModuleCount").ifPresent(c -> iTooltip.add(xlate("modularrouters.itemText.misc.moduleCount", c)));
//                data.getCompound("Upgrades").ifPresent(upgrades -> {
//                    iTooltip.add(xlate("modularrouters.itemText.misc.upgrades").append(":"));
//                    for (String k : upgrades.keySet()) {
//                        upgrades.getInt(k).ifPresent(c -> iTooltip.add(xlate("modularrouters.itemText.misc.upgradeCount", c, xlate(k))));
//                    }
//                });
//                RedstoneBehaviour rrb = RedstoneBehaviour.values()[data.getIntOr("RedstoneMode", 0)];
//                iTooltip.add(xlate("modularrouters.guiText.tooltip.redstone.label")
//                        .append(": " + ChatFormatting.AQUA)
//                        .append(xlate(rrb.getTranslationKey()))
//                );
//                data.getBoolean("EcoMode").ifPresent(b -> iTooltip.add(xlate("modularrouters.itemText.misc.ecoMode").withStyle(ChatFormatting.GREEN)));
//            }
//        }
//    }
//
//    @Override
//    public Identifier getUid() {
//        return RL("router");
//    }
//
//}
