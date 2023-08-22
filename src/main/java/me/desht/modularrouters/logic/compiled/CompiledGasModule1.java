package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.GasModule1;
import me.desht.modularrouters.util.ModuleHelper;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

import static me.desht.modularrouters.core.ModItems.GAS_HANDLER;
import static me.desht.modularrouters.item.module.GasModule1.GasDirection.IN;
import static me.desht.modularrouters.item.module.GasModule1.GasDirection.OUT;
import static mekanism.api.Action.EXECUTE;

public class CompiledGasModule1 extends CompiledModule {
    public static final String NBT_FORCE_EMPTY = "ForceEmpty";
    public static final String NBT_MAX_TRANSFER = "MaxTransfer";
    public static final String NBT_GAS_DIRECTION = "GasDir";
    public static final String NBT_REGULATE_ABSOLUTE = "RegulateAbsolute";

    public static final int BUCKET_VOLUME = 1000;

    private final int maxTransfer;
    private final GasModule1.GasDirection gasDirection;
    private final boolean forceEmpty;  // force emptying even if there's a gas block in the way
    private final boolean regulateAbsolute;  // true = regulate by mB; false = regulate by % of tank's capacity

    public CompiledGasModule1(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        CompoundTag compound = setupNBT(stack);
        maxTransfer = compound.getInt(NBT_MAX_TRANSFER);
        gasDirection = GasModule1.GasDirection.values()[compound.getByte(NBT_GAS_DIRECTION)];
        forceEmpty = compound.getBoolean(NBT_FORCE_EMPTY);
        regulateAbsolute = compound.getBoolean(NBT_REGULATE_ABSOLUTE);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (getTarget() == null) return false;
        Level world = Objects.requireNonNull(router.getLevel());
        BlockPos pos = getTarget().gPos.pos();
        // TODO selects the actual side, is that what you want?
        // changed so it selects the actual face you select when selecting the side on the block
        // instead of opposite/relative. I don't know if this is in line with the ideas about the mod though
        LazyOptional<IGasHandler> worldGasCap = ModItems.getGasHandler(world, pos, getTarget().face );
        Optional<IGasHandler> routerCap = router.getBuffer().getStackInSlot(0).getCapability(GAS_HANDLER).resolve();

        if ((!routerCap.isPresent()) || (!worldGasCap.isPresent())) return false;

        if (getRegulationAmount() > 0) {
            if (gasDirection == IN && worldGasCap.isPresent() && checkGasInTank(worldGasCap.resolve().get()) <= getRegulationAmount()) {
                return false;
            } else if (gasDirection == OUT && checkGasInTank(routerCap.get()) >= getRegulationAmount()) {
                return false;
            }
        }
        boolean didWork = false;
        if (worldGasCap.isPresent()) {
            didWork = switch (gasDirection) {
                case IN -> worldGasCap.map(srcHandler ->
                                routerCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, GasModule1.GasDirection.IN)).orElse(false))
                        .orElse(false);
                case OUT -> routerCap.map(srcHandler ->
                                worldGasCap.map(dstHandler -> doTransfer(router, srcHandler, dstHandler, GasModule1.GasDirection.OUT)).orElse(false))
                        .orElse(false);
            };
        }
        if (didWork) {
            //TODO fix this so it is in line with the fluid module
            // Couldn't really convert this reallly well (from how the fluid module does it) to a gas method so it doesn't really do much I think
            routerCap.ifPresent(h -> router.setBufferItemStack(router.getBufferItemStack()));
        }
        return didWork;
    }

    public static GasStack tryGasTransfer(IGasHandler gasDestination, IGasHandler gasSource, int maxAmount, boolean doTransfer)
    {
        long RTankSpace;
        // Mekanism seems to have some weird behaviour where it will actually keep extracting / inserting
        // when not possible.. found some really strange behaviour there.
        // Extra check and only continue with the amount of space left in the destination tank (if any)
        RTankSpace = (gasDestination.getTankCapacity(0) - gasDestination.getChemicalInTank(0).getAmount());
        GasStack drainable = gasSource.extractChemical(maxAmount, Action.SIMULATE);
        if ((RTankSpace < maxAmount) && (drainable.getAmount() > 0)) drainable.setAmount(RTankSpace);
        if (!drainable.isEmpty())
        {
            return tryGasTransfer_internal(gasDestination, gasSource, drainable, doTransfer);
        }
        return GasStack.EMPTY;
    }

    private static GasStack tryGasTransfer_internal(IGasHandler gasDestination, IGasHandler gasSource, GasStack drainable, boolean doTransfer)
    {
        GasStack fillableAmount = gasDestination.insertChemical(1, drainable, Action.SIMULATE);
        if (fillableAmount.getAmount() > 0)
        {
            drainable.setAmount(fillableAmount.getAmount());
            if (doTransfer)
            {
                GasStack drained = gasSource.extractChemical(drainable, EXECUTE);
                if (!drained.isEmpty())
                {
                    drained.setAmount(gasDestination.insertChemical(drained, EXECUTE).getAmount());
                    return drained;
                }
            }
            else
            {
                return drainable;
            }
        }
        return GasStack.EMPTY;
    }

    private boolean doTransfer(ModularRouterBlockEntity router, IGasHandler src, IGasHandler dest, GasModule1.GasDirection direction) {
        if (getRegulationAmount() > 0) {
            if (direction == IN && checkGasInTank(src) <= getRegulationAmount()) {
                return false;
            } else if (direction == OUT && checkGasInTank(dest) >= getRegulationAmount()) {
                return false;
            }
        }
        long amount = Math.min(maxTransfer, router.getCurrentGasTransferAllowance(direction));
        GasStack newStack = tryGasTransfer(dest, src, (int) amount, false);
        if (!newStack.isEmpty())  {
            newStack = tryGasTransfer(dest, src, (int) amount,  true);
            if (!newStack.isEmpty()) {
                router.transferredGas(newStack.getAmount(), direction);
                return true;
            }
        }
        return false;
    }

    private int checkGasInTank(IGasHandler handler) {
        // note: total amount of all gas in all tanks... not ideal for inventories with multiple tanks
        int total = 0, max = 0;

        if (isRegulateAbsolute()) {
            for (int idx = 0; idx < handler.getTanks(); idx++) {
                total += handler.getChemicalInTank(idx).getAmount();
            }
            return total;
        } else {
            for (int idx = 0; idx < handler.getTanks(); idx++) {
                max += handler.getTankCapacity(idx);
                total += handler.getChemicalInTank(idx).getAmount();
            }
            return max == 0 ? 0 : (total * 100) / max;

        }
    }

    private CompoundTag setupNBT(ItemStack stack) {
        CompoundTag compound = ModuleHelper.validateNBT(stack);
        if (!compound.contains(NBT_MAX_TRANSFER)) {
            compound.putInt(NBT_MAX_TRANSFER, BUCKET_VOLUME);
        }
        if (!compound.contains(NBT_GAS_DIRECTION)) {
            compound.putByte(NBT_GAS_DIRECTION, (byte) GasModule1.GasDirection.IN.ordinal());
        }
        return compound;
    }

    public GasModule1.GasDirection getGasDirection() {
        return gasDirection;
    }

    public int getMaxTransfer() {
        return maxTransfer;
    }

    public boolean isForceEmpty() {
        return forceEmpty;
    }

    public boolean isRegulateAbsolute() {
        return regulateAbsolute;
    }
}
