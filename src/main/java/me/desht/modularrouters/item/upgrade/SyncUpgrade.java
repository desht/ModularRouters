package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.gui.upgrade.GuiSyncUpgrade;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class SyncUpgrade extends ItemUpgrade {
    private static final String NBT_TUNING = "Tuning";

    @Override
    public void addExtraInformation(ItemStack itemstack, List<Component> list) {
        list.add(ClientUtil.xlate("modularrouters.itemText.sync.tuning", getTunedValue(itemstack)));
    }

    @Override
    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        router.setTunedSyncValue(getTunedValue(stack));
    }

    public static int getTunedValue(ItemStack stack) {
        if (!(stack.getItem() instanceof SyncUpgrade) || !stack.hasTag()) return 0;
        CompoundTag tag = stack.getTagElement(ModularRouters.MODID);
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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide && !player.isSteppingCarefully()) {
            GuiSyncUpgrade.openSyncGui(stack, hand);
        } else if (player.isSteppingCarefully()) {
            if (!world.isClientSide) {
                setTunedValue(stack, world.random.nextInt(MRConfig.Common.Router.baseTickRate));
                player.displayClientMessage(new TranslatableComponent("modularrouters.itemText.sync.tuning", getTunedValue(stack)), true);
            } else {
                player.playSound(ModSounds.SUCCESS.get(), 1.0f, 1.5f);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
