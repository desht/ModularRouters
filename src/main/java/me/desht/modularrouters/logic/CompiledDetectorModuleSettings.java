package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CompiledDetectorModuleSettings extends CompiledModuleSettings {
    public static final String NBT_SIGNAL_LEVEL = "SignalLevel";
    public static final String NBT_STRONG_SIGNAL = "StrongSignal";

    private final int signalLevel;
    private final boolean strongSignal;

    public CompiledDetectorModuleSettings(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        setupNBT(stack);
        NBTTagCompound compound = stack.getTagCompound();
        signalLevel = compound == null ? 0 : compound.getByte(NBT_SIGNAL_LEVEL);
        strongSignal = compound != null && compound.getBoolean(NBT_STRONG_SIGNAL);
    }

    private void setupNBT(ItemStack stack) {
        NBTTagCompound compound = Module.validateNBT(stack);
        if (!compound.hasKey(NBT_SIGNAL_LEVEL)) {
            compound.setInteger(NBT_SIGNAL_LEVEL, 15);
        }
        if (!compound.hasKey(NBT_STRONG_SIGNAL)) {
            compound.setBoolean(NBT_STRONG_SIGNAL, false);
        }
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public boolean isStrongSignal() {
        return strongSignal;
    }
}
