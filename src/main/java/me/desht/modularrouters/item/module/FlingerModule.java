package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.GuiModule;
import me.desht.modularrouters.gui.GuiModuleFlinger;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public class FlingerModule extends DropperModule {
    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.FLINGER),
                ItemModule.makeItemStack(ItemModule.ModuleType.DROPPER), Items.GUNPOWDER);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(itemstack, player, list, par4);
        CompiledFlingerModule fs = new CompiledFlingerModule(null, itemstack);
        list.add(I18n.format("itemText.misc.flingerDetails", fs.getSpeed(), fs.getPitch(), fs.getYaw()));
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter tileEntityItemRouter, ItemStack stack) {
        return new CompiledFlingerModule(tileEntityItemRouter, stack);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleFlinger.class;
    }
}
