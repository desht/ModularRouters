package me.desht.modularrouters.integration.waila;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class RouterComponentProvider implements IComponentProvider {
    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT data = accessor.getServerData();
        if (accessor.getTileEntity() instanceof TileEntityItemRouter) {
            if (data.getBoolean("Denied")) {
                tooltip.add(new TranslationTextComponent("modularrouters.chatText.security.accessDenied"));
            } else {
                MiscUtil.appendMultilineText(tooltip, TextFormatting.WHITE,"modularrouters.itemText.misc.moduleCount", data.getInt("ModuleCount"));
                CompoundNBT upgrades = data.getCompound("Upgrades");
                for (String k : upgrades.getAllKeys()) {
                    tooltip.add(new TranslationTextComponent("modularrouters.itemText.misc.upgradeCount", I18n.get(k), upgrades.getInt(k)));
                }
                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
                tooltip.add(new TranslationTextComponent("modularrouters.guiText.tooltip.redstone.label")
                        .append(": " + TextFormatting.AQUA)
                        .append(new TranslationTextComponent("modularrouters.guiText.tooltip.redstone." + rrb))
                );
                if (data.getBoolean("EcoMode")) {
                    tooltip.add(new TranslationTextComponent("modularrouters.itemText.misc.ecoMode"));
                }
            }
        }
    }
}
