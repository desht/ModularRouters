package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.upgrade.GuiSyncUpgrade;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

public class SyncUpgrade extends ItemUpgrade {
    private static final String NBT_TUNING = "Tuning";

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        list.add(ClientUtil.xlate("modularrouters.itemText.sync.tuning", getTunedValue(itemstack)));
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        router.setTunedSyncValue(getTunedValue(stack));
    }

    public static int getTunedValue(ItemStack stack) {
        if (!(stack.getItem() instanceof SyncUpgrade) || !stack.hasTag()) return 0;
        CompoundNBT tag = stack.getTagElement(ModularRouters.MODID);
        return tag == null ? 0 : tag.getInt(NBT_TUNING);
    }

    public static void setTunedValue(ItemStack stack, int newValue) {
        if (stack.getItem() instanceof SyncUpgrade) {
            stack.getOrCreateTagElement(ModularRouters.MODID).putInt(NBT_TUNING, newValue);
        }
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(255, 255, 192);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide && !player.isSteppingCarefully()) {
            GuiSyncUpgrade.openSyncGui(stack, hand);
        } else if (player.isSteppingCarefully()) {
            if (!world.isClientSide) {
                setTunedValue(stack, world.random.nextInt(MRConfig.Common.Router.baseTickRate));
            } else {
                player.displayClientMessage(ClientUtil.xlate("modularrouters.itemText.sync.tuning", getTunedValue(stack)), true);
                player.playSound(ModSounds.SUCCESS.get(), 1.0f, 1.5f);
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
