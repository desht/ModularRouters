package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.MRBaseItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class UpgradeItem extends MRBaseItem implements ModItems.ITintable {

    public UpgradeItem() {
        super(ModItems.defaultProps());
    }

    public TintColor getItemTint() {
        return TintColor.WHITE;
    }

    /**
     * Called when the router's upgrades are "compiled". <br>
     * Can be used to update buffer capacities, for instance.
     *
     * @param stack the upgrade stack
     * @param router the router
     */
    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        // no-op by default
    }

    /**
     * {@return the tag that will be sent for clients, to sync data}
     * @param router the router
     */
    @Nullable
    public CompoundTag createUpdateTag(ModularRouterBlockEntity router) {
        return null;
    }

    /**
     * Process an update packet.
     *
     * @param router the router
     * @param tag the update tag
     */
    public void processClientSync(ModularRouterBlockEntity router, @Nullable CompoundTag tag) {

    }

    protected void addExtraInformation(ItemStack stack, List<Component> list) {

    }

    /**
     * {@return {@code true} if this module can coexist with the {@code other} upgrade}
     */
    public boolean isCompatibleWith(UpgradeItem other) {
        return true;
    }

    /**
     * Get the maximum number of this upgrade that can be put in an upgrade slot
     * @param slot the slot number
     * @return the max number of upgrades
     */
    public int getStackLimit(int slot) {
        return 1;
    }
}
