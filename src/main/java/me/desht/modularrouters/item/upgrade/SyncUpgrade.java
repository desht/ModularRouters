package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.gui.upgrade.SyncUpgradeScreen;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

import static me.desht.modularrouters.client.util.ClientUtil.colorText;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class SyncUpgrade extends UpgradeItem {
    public SyncUpgrade(Properties properties) {
        super(properties.component(ModDataComponents.SYNC_TUNING, 1));
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, Consumer<Component> consumer) {
        int val = getTunedValue(itemstack);
        consumer.accept(xlate("modularrouters.itemText.sync.tuning", colorText(val, ChatFormatting.AQUA))
                .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        router.setTunedSyncValue(getTunedValue(stack));
    }

    public static int getTunedValue(ItemStack stack) {
        if (!(stack.getItem() instanceof SyncUpgrade)) return 0;
        return stack.getOrDefault(ModDataComponents.SYNC_TUNING.get(), 0);
    }

    public static void setTunedValue(ItemStack stack, int newValue) {
        if (stack.getItem() instanceof SyncUpgrade) {
            stack.set(ModDataComponents.SYNC_TUNING.get(), newValue);
        }
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(255, 255, 192);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide() && !player.isShiftKeyDown()) {
            SyncUpgradeScreen.openSyncGui(stack, hand);
        } else if (player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                setTunedValue(stack, world.random.nextInt(ConfigHolder.common.router.baseTickRate.get()));
                player.displayClientMessage(Component.translatable("modularrouters.itemText.sync.tuning", getTunedValue(stack)), true);
            } else {
                player.playSound(ModSounds.SUCCESS.get(), ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.5f);
            }
        }
        return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
