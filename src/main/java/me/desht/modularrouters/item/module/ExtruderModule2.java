package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModuleExtruder2;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledExtruder2Module;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.List;

public class ExtruderModule2 extends ItemModule implements IRangedModule {
    public ExtruderModule2(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruder2Module(router, stack);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        list.add(new TextComponentString(TextFormatting.YELLOW.toString()).appendSibling(new TextComponentTranslation("itemText.extruder2.template")));
        TemplateHandler handler = new TemplateHandler(itemstack);
        int size = list.size();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack blockStack = handler.getStackInSlot(i);
            if (!blockStack.isEmpty()) {
                list.add(new TextComponentString(" \u2022 " + TextFormatting.AQUA + blockStack.getCount() + " x " + blockStack.getDisplayName()));
            }
        }
        if (list.size() == size) {
            ITextComponent tc = list.get(size - 1);
            list.set(list.size() - 1,
                    tc.appendSibling(new TextComponentString(" " + TextFormatting.AQUA + TextFormatting.ITALIC))
                    .appendSibling(new TextComponentTranslation("itemText.misc.noItems"))
            );

        }
    }

    @Override
    public ContainerModule createContainer(EntityPlayer player, EnumHand hand, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerExtruder2Module(player.inventory, hand, moduleStack, router);
    }

    @Override
    public Class<? extends GuiModule> getGuiClass() {
        return GuiModuleExtruder2.class;
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.MODULE.extruder2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.MODULE.extruder2MaxRange.get();
    }

    @Override
    public Color getItemTint() {
        return new Color(227, 174, 27);
    }
}
