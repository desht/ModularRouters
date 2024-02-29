package me.desht.modularrouters.integration.waila;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.RL;

public class RouterComponentProvider implements IBlockComponentProvider {
    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag data = blockAccessor.getServerData();
        if (blockAccessor.getBlockEntity() instanceof ModularRouterBlockEntity) {
            if (data.getBoolean("Denied")) {
                iTooltip.add(xlate("modularrouters.chatText.security.accessDenied").withStyle(ChatFormatting.RED));
            } else {
                if (data.getInt("ModuleCount") > 0) {
                    iTooltip.add(xlate("modularrouters.itemText.misc.moduleCount", data.getInt("ModuleCount")));
                }
                CompoundTag upgrades = data.getCompound("Upgrades");
                if (!upgrades.isEmpty()) {
                    iTooltip.add(xlate("modularrouters.itemText.misc.upgrades"));
                    for (String k : upgrades.getAllKeys()) {
                        iTooltip.add(xlate("modularrouters.itemText.misc.upgradeCount", upgrades.getInt(k), xlate(k)));
                    }
                }
                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
                iTooltip.add(xlate("modularrouters.guiText.tooltip.redstone.label")
                        .append(": " + ChatFormatting.AQUA)
                        .append(xlate("modularrouters.guiText.tooltip.redstone." + rrb))
                );
                if (data.getBoolean("EcoMode")) {
                    iTooltip.add(xlate("modularrouters.itemText.misc.ecoMode").withStyle(ChatFormatting.GREEN));
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return RL("router");
    }

}
