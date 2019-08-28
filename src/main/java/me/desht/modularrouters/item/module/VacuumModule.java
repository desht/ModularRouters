package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.UniversalBucket;

import java.awt.*;
import java.util.List;

public class VacuumModule extends ItemModule implements IRangedModule {
    public VacuumModule(Properties props) {
        super(props);
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_VACUUM;
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVacuumModule(router, stack);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledVacuumModule cvm = new CompiledVacuumModule(null, itemstack);
        if (cvm.isXpMode()) {
            XPCollection.XPCollectionType type = cvm.getXPCollectionType();
            String modName = ModNameCache.getModName(type.getModId());
            String title = type.getIcon().getItem() instanceof UniversalBucket ?
                    MiscUtil.getFluidName(type.getIcon()) : type.getIcon().getDisplayName().getString();

            String s = I18n.format("guiText.label.xpVacuum") + ": "
                    + TextFormatting.AQUA + title + TextFormatting.BLUE + TextFormatting.ITALIC + " (" + modName + ")";
            list.add(MiscUtil.settingsStr(TextFormatting.GREEN.toString(), new StringTextComponent(s)));

            if (cvm.isAutoEjecting() && !type.isSolid()) {
                list.add(MiscUtil.settingsStr(TextFormatting.GREEN.toString(), new TranslationTextComponent("guiText.tooltip.xpVacuum.ejectFluid")));
            }
        }
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.MODULE.vacuumBaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.MODULE.vacuumMaxRange.get();
    }

    @Override
    public boolean isOmniDirectional() {
        return true;
    }

    @Override
    public Color getItemTint() {
        return new Color(120, 48, 191);
    }
}
