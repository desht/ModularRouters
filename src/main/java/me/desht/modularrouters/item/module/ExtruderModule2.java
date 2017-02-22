package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleExtruder2;
import me.desht.modularrouters.logic.compiled.CompiledExtruder2Module;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumHand;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExtruderModule2 extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruder2Module(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.EXTRUDER2),
                " c ", "ded", " p ",
                'c', Blocks.CHEST,
                'd', Items.DIAMOND,
                'e', ModuleHelper.makeItemStack(ItemModule.ModuleType.EXTRUDER),
                'p', Blocks.PISTON);
    }

    @Override
    public ContainerModule createGuiContainer(EntityPlayer player, EnumHand hand, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerExtruder2Module(player, hand, moduleStack, router);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleExtruder2.class;
    }
}
