package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule2;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.asFormattable;

public class ExtruderModule2 extends ItemModule implements IRangedModule {
    public ExtruderModule2() {
        super(ModItems.defaultProps());
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruderModule2(router, stack);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        list.add(new StringTextComponent(TextFormatting.YELLOW.toString()).append(ClientUtil.xlate("itemText.extruder2.template")));
        TemplateHandler handler = new TemplateHandler(itemstack, null);
        int size = list.size();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack blockStack = handler.getStackInSlot(i);
            if (!blockStack.isEmpty()) {
                list.add(new StringTextComponent(" \u2022 " + TextFormatting.AQUA + blockStack.getCount() + " x ").append(blockStack.getDisplayName()));
            }
        }
        if (list.size() == size) {
            ITextComponent tc = list.get(size - 1);
            list.set(list.size() - 1, asFormattable(tc)
                    .append(new StringTextComponent(" " + TextFormatting.AQUA + TextFormatting.ITALIC))
                    .append(ClientUtil.xlate("itemText.misc.noItems"))
            );

        }
    }

    @Override
    ContainerModule createContainer(int windowId, PlayerInventory invPlayer, MFLocator loc) {
        return new ContainerExtruder2Module(windowId, invPlayer, loc);
    }

    @Override
    public int getBaseRange() {
        return MRConfig.Common.Module.extruder2BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return MRConfig.Common.Module.extruder2MaxRange;
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(227, 174, 27);
    }
}
