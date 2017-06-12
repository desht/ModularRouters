package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleFlinger;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public class FlingerModule extends DropperModule {
    public static final float MIN_SPEED = 0.0f;
    public static final float MAX_SPEED = 20.0f;
    public static final float MIN_PITCH = -90.0f;
    public static final float MAX_PITCH = 90.0f;
    public static final float MIN_YAW = -60.0f;
    public static final float MAX_YAW = 60.0f;

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.FLINGER),
                ModuleHelper.makeItemStack(ItemModule.ModuleType.DROPPER), Items.GUNPOWDER);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
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
