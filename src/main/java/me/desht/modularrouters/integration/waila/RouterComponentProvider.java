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
                tooltip.add(new TranslationTextComponent("chatText.security.accessDenied"));
            } else {
                MiscUtil.appendMultilineText(tooltip, TextFormatting.WHITE,"itemText.misc.moduleCount", data.getInt("ModuleCount"));
                CompoundNBT upgrades = data.getCompound("Upgrades");
                for (String k : upgrades.keySet()) {
                    tooltip.add(new TranslationTextComponent("itemText.misc.upgradeCount", I18n.format(k), upgrades.getInt(k)));
                }
                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
                tooltip.add(new TranslationTextComponent("guiText.tooltip.redstone.label")
                        .appendString(": " + TextFormatting.AQUA)
                        .append(new TranslationTextComponent("guiText.tooltip.redstone." + rrb))
                );
                if (data.getBoolean("EcoMode")) {
                    tooltip.add(new TranslationTextComponent("itemText.misc.ecoMode"));
                }
            }
        }
    }
}
