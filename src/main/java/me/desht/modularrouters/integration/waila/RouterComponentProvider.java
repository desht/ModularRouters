package me.desht.modularrouters.integration.waila;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.RL;

public class RouterComponentProvider implements IBlockComponentProvider {
    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        CompoundTag data = blockAccessor.getServerData();
        if (blockAccessor.getBlockEntity() instanceof ModularRouterBlockEntity) {
            if (data.getBoolean("Denied")) {
                iTooltip.add(xlate("modularrouters.chatText.security.accessDenied"));
            } else {
                if (data.getInt("ModuleCount") > 0) {
                    List<Component> componentList = new ArrayList<>();
                    MiscUtil.appendMultilineText(componentList, ChatFormatting.WHITE, "modularrouters.itemText.misc.moduleCount", data.getInt("ModuleCount"));
                    iTooltip.addAll(componentList);
                }
                CompoundTag upgrades = data.getCompound("Upgrades");
                if (!upgrades.isEmpty()) {
                    iTooltip.add(xlate("modularrouters.itemText.misc.upgrades"));
                    for (String k : upgrades.getAllKeys()) {
                        iTooltip.add(xlate("modularrouters.itemText.misc.upgradeCount", upgrades.getInt(k), I18n.get(k)));
                    }
                }
                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
                iTooltip.add(xlate("modularrouters.guiText.tooltip.redstone.label")
                        .append(": " + ChatFormatting.AQUA)
                        .append(xlate("modularrouters.guiText.tooltip.redstone." + rrb))
                );
                if (data.getBoolean("EcoMode")) {
                    iTooltip.add(xlate("modularrouters.itemText.misc.ecoMode"));
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return RL("router");
    }

}
