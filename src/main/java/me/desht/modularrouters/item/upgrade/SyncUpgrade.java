package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.upgrade.GuiSyncUpgrade;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.ObjectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

public class SyncUpgrade extends ItemUpgrade {
    private static final String NBT_TUNING = "Tuning";

    public SyncUpgrade(Properties props) {
        super(props);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        list.add(new TextComponentTranslation("itemText.sync.tuning", getTunedValue(itemstack)));
    }

    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        router.setTunedSyncValue(getTunedValue(stack));
    }

    public static int getTunedValue(ItemStack stack) {
        if (!(stack.getItem() instanceof SyncUpgrade) || !stack.hasTag()) return 0;
        return stack.getTag().getInt(NBT_TUNING);
    }

    public static void setTunedValue(ItemStack stack, int newValue) {
        stack.getOrCreateTag().putInt(NBT_TUNING, newValue);
    }

    @Override
    public Color getItemTint() {
        return new Color(255, 255, 195);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote && !player.isSneaking()) {
            ModularRouters.proxy.openSyncGui(stack, hand);
        } else if (player.isSneaking()) {
            if (!world.isRemote) {
                setTunedValue(stack, world.rand.nextInt(ConfigHandler.ROUTER.baseTickRate.get()));
            } else {
                player.sendStatusMessage(new TextComponentTranslation("itemText.sync.tuning", getTunedValue(stack)), true);
                player.playSound(ObjectRegistry.SOUND_SUCCESS, 1.0f, 1.5f);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
