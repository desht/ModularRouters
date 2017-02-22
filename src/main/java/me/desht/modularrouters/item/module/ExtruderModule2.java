package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleExtruder2;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.compiled.CompiledExtruder2Module;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
    public Object[] getExtraUsageParams() {
        return new Object[]{Config.extruder2BaseRange, Config.extruder2MaxRange};
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.EXTRUDER2),
                " e ", "scp",
                'c', Blocks.CHEST,
                's', ItemModule.makeItemStack(ItemModule.ModuleType.SENDER1),
                'e', ItemModule.makeItemStack(ItemModule.ModuleType.EXTRUDER),
                'p', ItemModule.makeItemStack(ItemModule.ModuleType.PULLER));
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
        return Math.min(Config.extruder2BaseRange + (router == null ? 0 : router.getUpgradeCount(ItemUpgrade.UpgradeType.RANGE)), Config.extruder2MaxRange);
    }
}
