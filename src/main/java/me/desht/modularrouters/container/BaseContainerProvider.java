package me.desht.modularrouters.container;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;

import javax.annotation.Nullable;

public abstract class BaseContainerProvider implements IInteractionObject {

    @Override
    public ITextComponent getName() {
        return new TextComponentString(getGuiID());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }
}
