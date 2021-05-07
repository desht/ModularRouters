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

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class RouterComponentProvider implements IComponentProvider {
    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        CompoundNBT data = accessor.getServerData();
        if (accessor.getTileEntity() instanceof TileEntityItemRouter) {
            if (data.getBoolean("Denied")) {
                tooltip.add(xlate("modularrouters.chatText.security.accessDenied"));
            } else {
                if (data.getInt("ModuleCount") > 0) {
                    MiscUtil.appendMultilineText(tooltip, TextFormatting.WHITE, "modularrouters.itemText.misc.moduleCount", data.getInt("ModuleCount"));
                }
                CompoundNBT upgrades = data.getCompound("Upgrades");
                if (!upgrades.isEmpty()) {
                    tooltip.add(xlate("modularrouters.itemText.misc.upgrades"));
                    for (String k : upgrades.getAllKeys()) {
                        tooltip.add(xlate("modularrouters.itemText.misc.upgradeCount", upgrades.getInt(k), I18n.get(k)));
                    }
                }
                RouterRedstoneBehaviour rrb = RouterRedstoneBehaviour.values()[data.getInt("RedstoneMode")];
                tooltip.add(xlate("modularrouters.guiText.tooltip.redstone.label")
                        .append(": " + TextFormatting.AQUA)
                        .append(xlate("modularrouters.guiText.tooltip.redstone." + rrb))
                );
                if (data.getBoolean("EcoMode")) {
                    tooltip.add(xlate("modularrouters.itemText.misc.ecoMode"));
                }
            }
        }
    }
}
