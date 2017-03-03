package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleExtruder2;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.compiled.CompiledExtruder2Module;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;

public class ExtruderModule2 extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruder2Module(router, stack);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);

        list.add(TextFormatting.YELLOW + I18n.format("itemText.extruder2.template"));
        TemplateHandler handler = new TemplateHandler(stack);
        int size = list.size();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack blockStack = handler.getStackInSlot(i);
            if (!blockStack.isEmpty()) {
                list.add(" \u2022 " + TextFormatting.AQUA + blockStack.getCount() + " x " + blockStack.getDisplayName());
            }
        }
        if (list.size() == size) {
            String s = list.get(size - 1);
            list.set(size - 1, s + " " + TextFormatting.AQUA + TextFormatting.ITALIC + I18n.format("itemText.misc.noItems"));
        }
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[]{Config.extruder2BaseRange, Config.extruder2MaxRange};
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.EXTRUDER2),
                " e ", "scp",
                'c', "chestWood",
                's', ModuleHelper.makeItemStack(ItemModule.ModuleType.SENDER1),
                'e', ModuleHelper.makeItemStack(ItemModule.ModuleType.EXTRUDER),
                'p', ModuleHelper.makeItemStack(ItemModule.ModuleType.PULLER));
    }

    @Override
    public ContainerModule createGuiContainer(EntityPlayer player, EnumHand hand, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerExtruder2Module(player, hand, moduleStack, router);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleExtruder2.class;
    }

    @Override
    public boolean canBeRegulated() {
        return false;
    }

    public static int maxDistance(TileEntityItemRouter router) {
        return router == null ? Config.extruder2BaseRange : router.getEffectiveRange(Config.extruder2BaseRange, 1, Config.extruder2MaxRange);
    }
}
