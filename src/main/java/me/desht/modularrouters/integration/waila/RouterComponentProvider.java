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

import java.util.List;

public class RouterComponentProvider implements IComponentProvider {
    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT data = accessor.getServerData();
        if (accessor.getTileEntity() instanceof TileEntityItemRouter) {
            if (data.getBoolean("Denied")) {
                tooltip.add(MiscUtil.xlate("chatText.security.accessDenied"));
            } else {
                MiscUtil.appendMultilineText(tooltip, TextFormatting.WHITE,"itemText.misc.moduleCount", data.getInt("ModuleCount"));
                CompoundNBT upgrades = data.getCompound("Upgrades");
                for (String k : upgrades.keySet()) {
                    tooltip.add(MiscUtil.xlate("itemText.misc.upgradeCount", I18n.format(k), upgrades.getInt(k)));
                }
                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
                tooltip.add(MiscUtil.xlate("guiText.tooltip.redstone.label")
                        .appendText(": " + TextFormatting.AQUA)
                        .appendSibling(MiscUtil.xlate("guiText.tooltip.redstone." + rrb))
                );
                if (data.getBoolean("EcoMode")) {
                    tooltip.add(MiscUtil.xlate("itemText.misc.ecoMode"));
                }
            }
        }
    }
}
