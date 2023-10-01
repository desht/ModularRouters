package me.desht.modularrouters.container;

import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class TagFilterMenu extends AbstractInvSmartFilterMenu {
    public TagFilterMenu(int windowId, Inventory invPlayer, FriendlyByteBuf extraData) {
        super(ModMenuTypes.TAG_FILTER_MENU.get(), windowId, invPlayer, MFLocator.fromBuffer(extraData));
    }

    public TagFilterMenu(int windowId, Inventory invPlayer, MFLocator locator) {
        super(ModMenuTypes.TAG_FILTER_MENU.get(), windowId, invPlayer, locator);
    }
}
