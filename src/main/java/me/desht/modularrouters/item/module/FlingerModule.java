package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.TintColor;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
        list.add(new TranslationTextComponent("itemText.misc.flingerDetails", fs.getSpeed(), fs.getPitch(), fs.getYaw()));
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_FLINGER;
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter tileEntityItemRouter, ItemStack stack) {
        return new CompiledFlingerModule(tileEntityItemRouter, stack);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(230, 204, 240);
    }
}
