package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModuleFlinger;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.awt.*;
import java.util.List;

public class FlingerModule extends DropperModule {
    public static final float MIN_SPEED = 0.0f;
    public static final float MAX_SPEED = 20.0f;
    public static final float MIN_PITCH = -90.0f;
    public static final float MAX_PITCH = 90.0f;
    public static final float MIN_YAW = -60.0f;
    public static final float MAX_YAW = 60.0f;

    public FlingerModule(Properties props) {
        super(props);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);
        CompiledFlingerModule fs = new CompiledFlingerModule(null, itemstack);
        list.add(new TextComponentTranslation("itemText.misc.flingerDetails", fs.getSpeed(), fs.getPitch(), fs.getYaw()));
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter tileEntityItemRouter, ItemStack stack) {
        return new CompiledFlingerModule(tileEntityItemRouter, stack);
    }

    @Override
    public Class<? extends GuiModule> getGuiClass() {
        return GuiModuleFlinger.class;
    }

    @Override
    public Color getItemTint() {
        return new Color(230, 204, 240);
    }
}
